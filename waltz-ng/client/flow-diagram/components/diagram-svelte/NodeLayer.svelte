<script>
    import Node from "./Node.svelte";
    export let nodes = [];
    export let positions = {};
</script>

<g class="wfd-nodes">
    {#each nodes as node}
        <Node on:selectNode
              {node}
              {positions}/>
    {/each}
</g>
