<template>
  <v-app>
    <!-- Header Section -->
    <v-app-bar color="primary" class="px-4">
      <div class="header-left">
        <img src="/logo.png" alt="FocusFlow Logo" class="header-logo" />
        <h1 class="header-title">FocusFlow</h1>
      </div>
      <v-spacer></v-spacer>
      <v-btn color="white" variant="text" @click="goToLogin">
        <v-icon>mdi-arrow-left</v-icon>
        Back to Login
      </v-btn>
    </v-app-bar>

    <!-- Main Content Section -->
    <v-main>
      <v-container
        class="d-flex align-center justify-center"
        style="min-height: calc(100vh - 64px)"
      >
        <v-card class="mx-auto" max-width="450" elevation="8">
          <v-card-title class="text-h4 text-center pa-6 text-primary">
            Create Account
          </v-card-title>

          <v-card-text class="pa-6">
            <v-form @submit.prevent="handleRegister" ref="form">
              <v-row>
                <v-col cols="6">
                  <v-text-field
                    v-model="registerForm.firstName"
                    label="First Name"
                    prepend-inner-icon="mdi-account"
                    :rules="nameRules"
                    required
                    data-cy="first-name-input"
                  ></v-text-field>
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="registerForm.lastName"
                    label="Last Name"
                    :rules="nameRules"
                    required
                    data-cy="last-name-input"
                  ></v-text-field>
                </v-col>
              </v-row>

              <v-text-field
                v-model="registerForm.email"
                label="Email"
                type="email"
                prepend-inner-icon="mdi-email"
                :rules="emailRules"
                required
                class="mb-4"
                data-cy="email-input"
              ></v-text-field>

              <v-text-field
                v-model="registerForm.password"
                label="Password"
                :type="showPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click:append-inner="showPassword = !showPassword"
                :rules="passwordRules"
                required
                class="mb-4"
                data-cy="password-input"
              ></v-text-field>

              <v-text-field
                v-model="registerForm.confirmPassword"
                label="Confirm Password"
                :type="showConfirmPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-check"
                :append-inner-icon="
                  showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'
                "
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
                :rules="confirmPasswordRules"
                required
                class="mb-6"
                data-cy="confirm-password-input"
              ></v-text-field>

              <v-btn
                type="submit"
                color="primary"
                variant="elevated"
                size="large"
                block
                :loading="loading"
                class="mb-4"
                data-cy="register-button"
              >
                Create Account
              </v-btn>

              <div class="text-center">
                <span class="text-body-2">Already have an account? </span>
                <v-btn
                  color="primary"
                  variant="text"
                  @click="goToLogin"
                  data-cy="login-link"
                >
                  Sign In
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

        <!-- Success Alert -->
        <v-snackbar
          v-model="showSuccess"
          color="success"
          timeout="3000"
          location="top"
        >
          Account created successfully! Redirecting...
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
const showConfirmPassword = ref(false);
const showError = ref(false);
const showSuccess = ref(false);
const errorMessage = ref("");

const registerForm = ref({
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  confirmPassword: "",
});

// Validation rules
const nameRules = [
  (v) => !!v || "Name is required",
  (v) => v.length >= 2 || "Name must be at least 2 characters",
];

const emailRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email must be valid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
  (v) => v.length >= 6 || "Password must be at least 6 characters",
];

const confirmPasswordRules = [
  (v) => !!v || "Please confirm your password",
  (v) => v === registerForm.value.password || "Passwords do not match",
];

/**
 * Handle registration form submission
 */
const handleRegister = async () => {
  const { valid } = await form.value.validate();

  if (!valid) return;

  loading.value = true;
  try {
    await authService.register(
      registerForm.value.email,
      registerForm.value.password,
      registerForm.value.firstName,
      registerForm.value.lastName
    );

    showSuccess.value = true;

    // Redirect to main page after successful registration
    setTimeout(() => {
      router.push("/");
    }, 2000);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "Registration failed. Please try again.";
    showError.value = true;
  } finally {
    loading.value = false;
  }
};

/**
 * Navigate to login page
 */
const goToLogin = () => {
  router.push("/login");
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
