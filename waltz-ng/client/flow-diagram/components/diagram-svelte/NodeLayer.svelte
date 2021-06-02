<script>
    import Node from "./Node.svelte";
    export let nodes = [];
    export let positions = {};
</script>

<g class="wfd-nodes">
    {#each nodes as node}
        <Node {node}
              {positions}/>
    {/each}
</g>
