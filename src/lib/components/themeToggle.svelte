<script>
    import { theme } from "$lib/store/themeStore";
  
    function toggleTheme() {
        theme.update((current) => (current === "focusflowLight" ? "focusflowDark" : "focusflowLight"));
    }
</script>

<button class="btn btn-outline" on:click={toggleTheme}>
    Toggle Theme
</button>
  