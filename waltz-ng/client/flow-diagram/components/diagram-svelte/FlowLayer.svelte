<script>
    import Flow from "./Flow.svelte";

    export let model;
    export let positions;
</script>

<g class="wfd-flows">
    {#each $model.flows as flow}
        <Flow on:selectFlow
              sourcePos={$positions[flow.source]}
              targetPos={$positions[flow.target]}
              decorations={$model.decorations}
              {flow}/>
    {/each}
</g>
