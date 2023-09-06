<script>
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";

    export let cellData = {};

    let references = [];

    $: references = cellData?.backingEntityReferences || [];

</script>


<div style="align-content: center; text-align: center">
    {#each references as reference}
        <EntityLink ref={reference}
                showIcon={false}/>
    {/each}
</div>
