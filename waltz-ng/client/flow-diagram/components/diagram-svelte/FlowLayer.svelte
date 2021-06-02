<script>
    import Flow from "./Flow.svelte";

    export let positions = {};
    export let decorations = {};
    export let flows = [];
</script>

{#each flows as flow}
    <Flow on:selectFlow
          {positions}
          {decorations}
          {flow}/>
{/each}
