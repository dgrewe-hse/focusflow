import { createRouter, createWebHistory } from "vue-router";
import TaskList from "../views/TaskListPage.vue";
import TaskForm from "../views/TaskFormPage.vue";
import Login from "../views/LoginPage.vue";
import Register from "../views/RegisterPage.vue";
import { authService } from "../services/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "Login",
      component: Login,
      meta: { requiresGuest: true },
    },
    {
      path: "/register",
      name: "Register",
      component: Register,
      meta: { requiresGuest: true },
    },
    {
      path: "/",
      name: "TaskList",
      component: TaskList,
      meta: { requiresAuth: true },
    },
    {
      path: "/task/create",
      name: "TaskCreate",
      component: TaskForm,
      meta: { requiresAuth: true },
    },
  ],
});

// Navigation guards
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated;

  // Routes that require authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next("/login");
    return;
  }

  // Routes that require guest (not authenticated)
  if (to.meta.requiresGuest && isAuthenticated) {
    next("/");
    return;
  }

  next();
});

export default router;
