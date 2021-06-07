<script>
    import Annotation from "./Annotation.svelte";

    export let positions = {};
    export let annotations = [];
</script>

<g class="annotations">
    {#each annotations as annotation}
       <Annotation on:selectAnnotation
                   {annotation}
                   {positions}/>
    {/each}
</g>
