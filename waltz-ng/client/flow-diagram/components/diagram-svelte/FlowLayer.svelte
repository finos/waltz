<script>
    import Flow from "./Flow.svelte";

    export let positions = {};
    export let decorations = {};
    export let flows = [];
</script>

<g class="wfd-flows">
    {#each flows as flow}
        <Flow on:selectFlow
              {positions}
              {decorations}
              {flow}/>
    {/each}
</g>
