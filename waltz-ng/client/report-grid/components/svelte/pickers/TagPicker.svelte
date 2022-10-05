<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let onSelect = (tagColumn) => console.log("Selecting tagColumn");
    export let selectionFilter = () => true;

    const tagColumn = {
        kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
        columnEntityId: null,
        columnEntityKind: "TAG",
        entityFieldReference: null,
        columnName: "Tags",
        displayName: null
    };

    $: canAddTags = selectionFilter(tagColumn);

</script>

<div style="padding-top: 1em">
    {#if canAddTags}
        <button class="btn btn-skinny"
                on:click={() => onSelect(tagColumn)}>
            <Icon name="plus"/>
            Add tags to grid
        </button>
    {:else}
        <NoData type="info">Tags column added to report grid</NoData>
    {/if}
</div>