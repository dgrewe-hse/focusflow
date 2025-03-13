# Documentation

A lightweight web-application to plan your schedule.

---

## Technology decision

- Frontend: SvelteKit
- CSS Framework: TailwindCSS, daisyUI
- Backend: Node.js
- ORM: Prisma
- Database: PostgreSQL
- Deployment: Docker

---

## Setup

1. Install dependencies with `npm install`
2. Create an `.env`. It should contain the following:
    ```dotenv
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=postgres
    POSTGRES_DB=postgres
    DATABASE_URL="postgresql://$POSTGRES_USER:$POSTGRES_PASSWORD@localhost:42187/$POSTGRES_DB?schema=public"
    ```
3. Start the development server with `npm run dev`
4. If you want to have a database running, you can use `docker-compose up db -d` to start a postgres database.
