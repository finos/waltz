<script>
    import Flow from "./Flow.svelte";

    export let model;
    export let positions;

    $: console.log("model", {model})
</script>

<g class="wfd-flows">
    {#each $model.flows as flow}
<!--        <text dy="100" dx="100">Foo</text>-->
        <Flow on:selectFlow
              positions={$positions}
              decorations={$model.decorations}
              {flow}/>
    {/each}
</g>
