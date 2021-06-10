<script>
    import Flow from "./Flow.svelte";

    export let model;
    export let positions;

    /*
            const sourcePos = positions[f.source];
        const targetPos = positions[f.target];

     */
    $: console.log("model", {model})
</script>

<g class="wfd-flows">
    {#each $model.flows as flow}
<!--        <text dy="100" dx="100">Foo</text>-->
        <Flow on:selectFlow
              sourcePos={$positions[flow.source]}
              targetPos={$positions[flow.target]}
              decorations={$model.decorations}
              {flow}/>
    {/each}
</g>
