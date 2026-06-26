import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles/common.css'
import axios from "axios";
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { API_BASE_URL } from './api/baseUrl'

const app = createApp(App)
axios.defaults.baseURL = API_BASE_URL

app.use(router)
app.use(ElementPlus)
app.mount('#app')
