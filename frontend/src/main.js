import { createApp } from "vue";
import App from "./App.vue";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import "vuetify/styles";
import "@mdi/font/css/materialdesignicons.css";
import router from "./router";

const vuetify = createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        colors: {
          primary: "#009688", // Teal color from logo
          secondary: "#00695C", // Darker teal
          accent: "#4DB6AC", // Light teal
          error: "#F44336",
          warning: "#FF9800",
          info: "#2196F3",
          success: "#4CAF50",
          surface: "#FFFFFF",
          background: "#F5F5F5",
        },
      },
    },
  },
  defaults: {
    VBtn: {
      variant: "text",
      density: "comfortable",
    },
    VTextField: {
      variant: "outlined",
      density: "comfortable",
      color: "primary",
      bgColor: "white",
    },
    VTextarea: {
      variant: "outlined",
      density: "comfortable",
      color: "primary",
      bgColor: "white",
    },
    VSelect: {
      variant: "outlined",
      density: "comfortable",
    },
  },
});

const app = createApp(App);
app.use(vuetify);
app.use(router);
app.mount("#app");
