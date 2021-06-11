<script>
    import Flow from "./Flow.svelte";
    import {toGraphId} from "../../flow-diagram-utils";

    export let model;
    export let positions;
</script>

<g class="wfd-flows">
    {#each $model.flows as flow}
        <Flow on:selectFlow
              sourcePos={positions[toGraphId(flow.source)]}
              targetPos={positions[toGraphId(flow.target)]}
              decorations={$model.decorations}
              {flow}/>
    {/each}
</g>
