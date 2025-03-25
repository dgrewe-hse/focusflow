<script lang="ts">
    import { enhance } from "$app/forms";
    import { theme } from "$lib/store/themeStore";
    import { onMount } from "svelte";
    import ThemeToggle from "./themeToggle.svelte";

	function logoutCallback() {
		localStorage.removeItem("SessionInitalized");
	}

	onMount(() => {
		document.documentElement.setAttribute("data-theme", $theme);
	});
</script>

<div class="navbar bg-base-100 shadow-md">
	<div class="navbar-start">
	<div class="dropdown">
		<div tabindex="0" role="button" class="btn btn-ghost btn-circle">
		<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"> <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" /> </svg>
		</div>
		<ul class="menu menu-sm dropdown-content bg-base-100 rounded-box z-1 mt-3 w-52 p-2 shadow border border-base200">
			<li><a href="/">Home</a></li>
			<li><a href="/settings">Settings</a></li>
		</ul>
	</div>
	</div>
	<div class="navbar-center">
		<a href="/" class="btn btn-ghost text-xl text-primary">FocusFlow</a>
	</div>
	<div class="navbar-end">
		<ThemeToggle/>
		<form method="post" action="?/logout" use:enhance = {() => {
				logoutCallback();
				return async ({ update }) => {
					await update();
				};
			}}>
			<button class="btn btn-ghost rounded-lg">Sign out</button>
		</form>
		<!-- <button class="btn btn-ghost btn-circle">
			<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"> <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /> </svg>
		</button>
		<button class="btn btn-ghost btn-circle">
			<div class="indicator">
			<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"> <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /> </svg>
			<span class="badge badge-xs badge-primary indicator-item"></span>
			</div>
		</button> -->
	</div>
</div>