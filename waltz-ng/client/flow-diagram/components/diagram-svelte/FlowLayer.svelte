<script>
    import Flow from "./Flow.svelte";

    export let positions = {};
    export let flows = [];
</script>

{#each flows as flow}
    <Flow {positions}
          {flow}/>
{/each}
