<script>
    import Annotation from "./Annotation.svelte";

    export let positions = {};
    export let annotations = [];
</script>

<g class="annotations">
    {#each annotations as annotation}
       <Annotation {annotation}
                   {positions}/>
    {/each}
</g>
