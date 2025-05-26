<template>
  <v-app>
    <!-- Main application container -->
    <router-view></router-view>
  </v-app>
</template>

<script setup>
// Using script setup for Vue 3 Composition API
defineOptions({
  name: "App",
});
</script>

<style>
/* Global styles */
:root {
  --primary-color: #009688; /* Teal color from logo */
  --secondary-color: #00695c; /* Darker teal */
  --accent-color: #4db6ac; /* Light teal */
  --error-color: #f44336;
}

/* Ensure the app takes full height */
html,
body {
  height: 100%;
  margin: 0;
  background-color: #f5f5f5;
}

.v-application {
  min-height: 100vh;
}

/* Custom teal styling for components */
.v-btn--variant-elevated.v-btn--color-primary {
  background-color: var(--primary-color) !important;
}

.v-btn--variant-text.v-btn--color-primary {
  color: var(--primary-color) !important;
}
</style>
