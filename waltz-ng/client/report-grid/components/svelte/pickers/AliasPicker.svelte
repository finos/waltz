<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let onSelect = (aliasColumn) => console.log("Selecting aliasColumn");
    export let selectionFilter = () => true;

    const aliasesColumn = {
        columnEntityId: null,
        columnEntityKind: "ENTITY_ALIAS",
        entityFieldReference: null,
        columnName: "Aliases",
        displayName: null
    };

    $: canAddAliases = selectionFilter(aliasesColumn);

</script>

<div style="padding-top: 1em">
    {#if canAddAliases}
        <button class="btn btn-skinny"
                on:click={() => onSelect(aliasesColumn)}>
            <Icon name="plus"/>
            Add aliases to grid
        </button>
    {:else}
        <NoData type="info">Aliases column added to report grid</NoData>
    {/if}
</div>