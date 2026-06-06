import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import LobbyView from './views/LobbyView.vue'
import GameView from './views/GameView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: LoginView },
    { path: '/lobby', component: LobbyView },
    { path: '/game', component: GameView },
  ],
})
