# Documentation

A lightweight web-application to plan your schedule.

## Technology decision

We use the framework SvelteKit because we have worked with it a lot in the past.

Svelte provides frontend as well as backend. To work with it the server files just have to be tagged with an additional .server extension.</br>
PostgresSQL is chosen as a powerful and common database.

## Team Roles

Roles which are mainly focused on their topics:

* Frontend-Master: ...
* Backend-Master: ...
* Database-Master: ...
* QA-Tester: ...

## Developing

Install dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of the app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.
