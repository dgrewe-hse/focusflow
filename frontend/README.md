# FocusFlow Frontend

A Vue.js 3 frontend application for the FocusFlow task management system.

## Features

- **Modern Vue.js 3** with Composition API
- **Vuetify 3** for Material Design components
- **Vite** for fast development and building
- **Teal color theme** matching the FocusFlow logo
- **Responsive design** with modern UI/UX
- **Cypress** for end-to-end testing

## Development Setup

### Prerequisites

- Node.js 20.x or higher
- npm or yarn package manager

### Installation

1. Navigate to the frontend directory:

   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

### Running the Application

#### Development Server

Start the development server on port 8081:

```bash
npm run dev
# or
npm run serve
```

The application will be available at: `http://localhost:8081`

#### Production Build

Build the application for production:

```bash
npm run build
```

#### Preview Production Build

Preview the production build locally:

```bash
npm run preview
```

### Testing

Run Cypress end-to-end tests:

```bash
npm run test
```

## Configuration

- **Frontend Port**: 8081 (configured in `vite.config.js`)
- **Backend API**: http://127.0.0.1:8080 (configured in `src/services/api.js`)
- **Theme**: Teal color scheme matching the FocusFlow logo

## Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── logo.png          # FocusFlow logo
├── src/
│   ├── router/           # Vue Router configuration
│   ├── services/         # API service layer
│   ├── views/            # Vue components/pages
│   ├── App.vue           # Main application component
│   └── main.js           # Application entry point
├── cypress/              # End-to-end tests
├── vite.config.js        # Vite configuration
└── package.json          # Dependencies and scripts
```

## API Integration

The frontend communicates with the Java Spring Boot backend running on port 8080. All API calls are handled through the centralized API service in `src/services/api.js`.

## Styling

The application uses a custom teal color theme that matches the FocusFlow logo:

- Primary: #009688 (Teal)
- Secondary: #00695C (Darker teal)
- Accent: #4DB6AC (Light teal)
