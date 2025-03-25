<script lang="ts">
    import Banner from "$lib/components/banner.svelte";
    import Header from "$lib/components/header.svelte";
    import { onMount } from "svelte";

	import type { PageData } from "./$types";

	export let data: PageData;

	let firstLogin : boolean;
	onMount(() => {
		const isSessionInit = localStorage.getItem("SessionInitalized");
		if(isSessionInit == null || isSessionInit == "false") {
			localStorage.setItem("SessionInitalized", "true");
			firstLogin = true;
		} else {
			firstLogin = false;
		}
	});
</script>

<Header/>
<main class="w-full">
	{#if firstLogin == true}
		<Banner visibleTime={2600} fadeIn={600} fadeOut={750}>
			<p>Welcome {data.user.username}!</p>
		</Banner>		
	{/if}

	<p>
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Molestiae laudantium soluta dolor dolorem, ex, voluptates fugit ea quam minima tempore ab similique saepe velit officiis consequatur cupiditate voluptatibus perferendis est.
	</p>
</main>