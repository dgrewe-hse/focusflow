<template>
  <v-app>
    <!-- Header Section -->
    <v-app-bar color="primary" class="px-4">
      <div class="header-left">
        <img src="/logo.png" alt="FocusFlow Logo" class="header-logo" />
        <h1 class="header-title">FocusFlow</h1>
      </div>
      <v-spacer></v-spacer>
    </v-app-bar>

    <!-- Main Content Section -->
    <v-main>
      <v-container
        class="d-flex align-center justify-center"
        style="min-height: calc(100vh - 64px)"
      >
        <v-card class="mx-auto" max-width="400" elevation="8">
          <v-card-title class="text-h4 text-center pa-6 text-primary">
            Welcome Back
          </v-card-title>

          <v-card-text class="pa-6">
            <v-form @submit.prevent="handleLogin" ref="form">
              <v-text-field
                v-model="loginForm.email"
                label="Email"
                type="email"
                prepend-inner-icon="mdi-email"
                :rules="emailRules"
                required
                class="mb-4"
                data-cy="email-input"
              ></v-text-field>

              <v-text-field
                v-model="loginForm.password"
                label="Password"
                :type="showPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click:append-inner="showPassword = !showPassword"
                :rules="passwordRules"
                required
                class="mb-6"
                data-cy="password-input"
              ></v-text-field>

              <v-btn
                type="submit"
                color="primary"
                variant="elevated"
                size="large"
                block
                :loading="loading"
                class="mb-4"
                data-cy="login-button"
              >
                Sign In
              </v-btn>

              <div class="text-center">
                <span class="text-body-2">Don't have an account? </span>
                <v-btn
                  color="primary"
                  variant="text"
                  @click="goToRegister"
                  data-cy="register-link"
                >
                  Sign Up
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>

        <!-- Error Alert -->
        <v-snackbar
          v-model="showError"
          color="error"
          timeout="5000"
          location="top"
        >
          {{ errorMessage }}
          <template v-slot:actions>
            <v-btn color="white" variant="text" @click="showError = false">
              Close
            </v-btn>
          </template>
        </v-snackbar>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { authService } from "../services/auth";

const router = useRouter();
const form = ref(null);
const loading = ref(false);
const showPassword = ref(false);
const showError = ref(false);
const errorMessage = ref("");

const loginForm = ref({
  email: "",
  password: "",
});

// Validation rules
const emailRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email must be valid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
  (v) => v.length >= 6 || "Password must be at least 6 characters",
];

/**
 * Handle login form submission
 */
const handleLogin = async () => {
  const { valid } = await form.value.validate();

  if (!valid) return;

  loading.value = true;
  try {
    await authService.login(loginForm.value.email, loginForm.value.password);

    // Redirect to main page on successful login
    router.push("/");
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message ||
      "Login failed. Please check your credentials.";
    showError.value = true;
  } finally {
    loading.value = false;
  }
};

/**
 * Navigate to registration page
 */
const goToRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
/* Header styles */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo {
  height: 32px;
  width: auto;
}

.header-title {
  color: white;
  font-size: 20px;
  font-weight: 500;
  margin: 0;
  letter-spacing: 0.5px;
}

/* Card styling */
.v-card {
  border-radius: 12px;
}

.v-card-title {
  background: linear-gradient(135deg, #009688 0%, #00695c 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
