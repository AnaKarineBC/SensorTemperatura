const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const path = require('path');

// Inicializar Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');


admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://console.firebase.google.com/u/1/project/finalproject-c4d5d/firestore/databases/-default-/data/~2FSensorData~2F127688?hl=pt-br"
});

const db = admin.firestore();

// Configuração do Express
const app = express();
app.use(bodyParser.urlencoded({ extended: true })); // Para parsear dados de formulários
app.use(express.static(path.join(__dirname, 'public')));
// Configurar a engine de visualização
app.set('view engine', 'ejs');
app.set('views', './views');

const port = 8000;

// Rota GET para exibir todos os dados
app.get('/', async (req, res) => {
    try {
        const snapshot = await db.collection('SensorData').get();
        const allData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        res.render('index', { data: allData, searchResults: null, error: null });
    } catch (error) {
        console.error("Erro ao buscar todos os dados:", error);
        res.render('index', { data: [], searchResults: null, error: "Erro ao buscar dados." });
    }
});

// Rota POST para buscar dados por data
app.post('/search', async (req, res) => {
    const { date } = req.body; // Data inserida pelo usuário no formato YYYY-MM-DD

    if (!date || !/^\d{4}-\d{2}-\d{2}$/.test(date)) { // Validação para YYYY-MM-DD
        return res.render('index', {
            data: [],
            searchResults: [],
            error: "Por favor, insira uma data válida no formato YYYY-MM-DD."
        });
    }

    try {
        // Buscar dados no Firestore onde o campo 'date' corresponde à data fornecida
        const snapshot = await db.collection('SensorData').where('date', '==', date).get();

        if (snapshot.empty) {
            return res.render('index', {
                data: [],
                searchResults: [],
                error: "Nenhum dado encontrado para a data especificada."
            });
        }

        // Mapear os resultados encontrados
        const searchResults = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        // Buscar todos os dados para exibir na tabela geral
        const allDataSnapshot = await db.collection('sensor').get();
        const allData = allDataSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        res.render('index', { data: allData, searchResults, error: null });
    } catch (error) {
        console.error("Erro ao buscar dados:", error);
        res.render('index', { data: [], searchResults: null, error: "Erro ao buscar dados." });
    }
});

// Inicializar o servidor
app.listen(port, () => {
    console.log(`Servidor rodando em http://localhost:${port}`);
});
