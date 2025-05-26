# Authentication Integration - Frontend & Backend

This document describes the complete authentication integration between the Vue.js frontend and Spring Boot backend for the FocusFlow application.

## 🔐 Authentication Flow

### Backend (Spring Boot)

- **JWT Token-based authentication** using Spring Security
- **AuthController** with `/api/v1/auth/login` and `/api/v1/auth/register` endpoints
- **TaskController** with `@SecurityRequirement(name = "bearerAuth")` and `@PreAuthorize("hasRole('USER')")`
- **Bearer token authentication** required for all task operations

### Frontend (Vue.js)

- **Reactive authentication state** using Vue 3 Composition API
- **JWT token management** with localStorage persistence
- **Route guards** for protected routes
- **Automatic token injection** in API requests
- **401 error handling** with automatic logout

## 🚀 Features Implemented

### 1. Authentication Service (`src/services/auth.js`)

- ✅ Login with email/password
- ✅ User registration with first name, last name, email, password
- ✅ JWT token storage and management
- ✅ Reactive authentication state
- ✅ Automatic logout on token expiration

### 2. API Integration (`src/services/api.js`)

- ✅ Automatic JWT token injection in request headers
- ✅ 401 error handling with redirect to login
- ✅ Request/response interceptors for debugging

### 3. User Interface

- ✅ **Login Page** (`/login`) - Email/password authentication
- ✅ **Register Page** (`/register`) - User registration form
- ✅ **Protected Routes** - Task list and creation require authentication
- ✅ **Logout functionality** in header with user email display

### 4. Task Management Integration

- ✅ **TaskDTO structure** matching backend requirements
- ✅ **API Response wrapper** handling (`ApiResponse<T>`)
- ✅ **Task CRUD operations** with proper authentication
- ✅ **Status and Priority** display with color coding

## 📋 API Endpoints Integration

### Authentication Endpoints

```javascript
// Login
POST / api / v1 / auth / login;
Body: {
  email, password;
}
Response: {
  token, email, userId;
}

// Register
POST / api / v1 / auth / register;
Body: {
  email, password, firstName, lastName;
}
Response: {
  token, email, userId;
}
```

### Task Endpoints (Authenticated)

```javascript
// Get all tasks
GET /api/v1/tasks
Headers: { Authorization: "Bearer <token>" }

// Create task
POST /api/v1/tasks
Headers: { Authorization: "Bearer <token>" }
Body: { title, shortDescription, longDescription, dueDate, priority, createdById }

// Update task
PUT /api/v1/tasks/{id}
Headers: { Authorization: "Bearer <token>" }
Body: { title, shortDescription, ... }

// Delete task
DELETE /api/v1/tasks/{id}
Headers: { Authorization: "Bearer <token>" }
```

## 🛠️ Technical Implementation

### Route Guards

```javascript
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated;

  if (to.meta.requiresAuth && !isAuthenticated) {
    next("/login");
  } else if (to.meta.requiresGuest && isAuthenticated) {
    next("/");
  } else {
    next();
  }
});
```

### JWT Token Management

```javascript
// Automatic token injection
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 401 error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```

## 🎨 UI/UX Features

### Design Consistency

- ✅ **Teal color theme** matching FocusFlow logo
- ✅ **Vuetify Material Design** components
- ✅ **Responsive layout** for all screen sizes
- ✅ **Logo integration** in all page headers

### User Experience

- ✅ **Form validation** with real-time feedback
- ✅ **Loading states** during API calls
- ✅ **Error handling** with user-friendly messages
- ✅ **Success notifications** for completed actions
- ✅ **Password visibility toggle** in forms

## 🧪 Testing the Integration

### 1. Start the Backend

```bash
# Ensure your Spring Boot backend is running on port 8080
# with the AuthController and TaskController endpoints
```

### 2. Start the Frontend

```bash
cd frontend
npm install
npm run dev
# Frontend will run on http://localhost:8081
```

### 3. Test Authentication Flow

1. **Visit** `http://localhost:8081` (should redirect to `/login`)
2. **Register** a new account via "Sign Up" link
3. **Login** with your credentials
4. **Create tasks** using the "CREATE NEW TASK" button
5. **View tasks** in the main dashboard with status/priority
6. **Edit/Delete** tasks using the action buttons
7. **Logout** using the logout button in header

### 4. Test API Integration

- Open browser DevTools → Network tab
- Observe API calls with proper `Authorization: Bearer <token>` headers
- Verify task CRUD operations work correctly
- Test token expiration handling (manually clear token from localStorage)

## 🔧 Configuration

### Frontend Configuration

- **Port**: 8081 (configured in `vite.config.js`)
- **Backend API**: http://127.0.0.1:8080 (configured in `src/services/api.js`)
- **Theme**: Teal color scheme (#009688)

### Backend Requirements

- **JWT Token Provider** configured
- **CORS** enabled for frontend origin
- **Security configuration** allowing `/api/v1/auth/**` endpoints
- **User roles** properly configured

## 🚨 Security Considerations

### Frontend Security

- ✅ **JWT tokens** stored in localStorage (consider httpOnly cookies for production)
- ✅ **Automatic logout** on token expiration
- ✅ **Route protection** for authenticated pages
- ✅ **Input validation** on all forms

### Backend Security

- ✅ **JWT token validation** on protected endpoints
- ✅ **Role-based access control** with `@PreAuthorize`
- ✅ **Password encryption** (handled by Spring Security)
- ✅ **CORS configuration** for frontend integration

## 📝 Next Steps

### Potential Enhancements

- [ ] **Refresh token** implementation for better security
- [ ] **Remember me** functionality
- [ ] **Password reset** flow
- [ ] **User profile** management
- [ ] **Role-based UI** features
- [ ] **Task assignment** to other users
- [ ] **Real-time updates** with WebSockets

### Production Considerations

- [ ] **Environment variables** for API URLs
- [ ] **HTTPS** configuration
- [ ] **Error logging** and monitoring
- [ ] **Performance optimization**
- [ ] **Security headers** configuration
