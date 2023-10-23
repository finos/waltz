<script>
    import DateTime from "./DateTime.svelte";
    export let entity;
    export let showLabel = false;
</script>

{#if entity}
<span>
    {#if showLabel}
    <span class="text-muted">Last Updated: </span>
    {/if}
    {entity.lastUpdatedBy},
    <DateTime relative={true}
              formatStr="yyyy-MM-DD HH:mm:ss"
              dateTime={entity.lastUpdatedAt}/>
</span>
{/if}
