// Importa o CSS do Bootstrap para o site ficar bonito
import 'bootstrap/dist/css/bootstrap.min.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // Vamos configurar esse cara no próximo passo

const app = createApp(App)

// Avisa o Vue para usar o sistema de rotas (páginas)
app.use(router)

// Monta o site na tela
app.mount('#app')