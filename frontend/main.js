import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles/common.css'
import axios from "axios";
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)
axios.defaults.baseURL='http://localhost:8080'

app.use(router)
app.mount('#app')

