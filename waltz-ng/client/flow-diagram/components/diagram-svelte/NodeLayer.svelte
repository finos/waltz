<script>
    import Node from "./Node.svelte";
    export let nodes = [];
    export let positions = {};
</script>

{#each nodes as node}
    <Node {node}
          {positions}/>
{/each}