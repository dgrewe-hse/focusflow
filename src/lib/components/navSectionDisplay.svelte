<script lang="ts">
    import { navigationSections, navSection } from "$lib/store/navSectionStore";

    const section = $navSection;
    let previousSections = [];
    let nextSections = [];
    for(let i = 0; i < navigationSections.length; i++) {
        if(navigationSections[i][0] == section) {
            nextSections.push(navigationSections.slice(i + 1, navigationSections.length).map((_section) => _section[0]));
            break;
        }
        previousSections.push(navigationSections[i]);
    }

</script>

<!-- Make an arcave bow below the header which indicates the current navigation -->
<div class="flex place-self-center m-2 mt-0 gap-2">

    {#each previousSections as previousSection}
        <p>{previousSection[0]}</p>
    {/each}

    <p class="underline">{section}</p>

    {#each nextSections as nextSection}
        <p>{nextSection[0]}</p>
    {/each}

</div>