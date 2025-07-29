<script>
    export let parentEntityRef;
</script>
<div>
    {#if parentEntityRef}
        {parentEntityRef.id}: {parentEntityRef.kind}
    {/if}
    Hello, Data Flow
</div>