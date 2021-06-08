<script>
    import Node from "./Node.svelte";

    export let nodes = [];
    export let positions = {};
    export let groups = [];
</script>

<g class="wfd-nodes">
    {#each nodes as node}
        <Node on:selectNode
              {node}
              {positions}
              {groups}/>
    {/each}
</g>
