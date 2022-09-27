<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {entityFieldReferenceStore} from "../../../../svelte-stores/entity-field-reference-store";
    import {entity} from "../../../../common/services/enums/entity";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let onSelect = () => console.log("Selecting change initiative field");
    export let selectionFilter = () => true;

    $: entityFieldReferenceCall = entityFieldReferenceStore.findAll();
    $: entityFieldReferences = $entityFieldReferenceCall.data;

    $: fieldReferences = _
        .chain(entityFieldReferences)
        .filter(d => d.entityKind === "CHANGE_INITIATIVE")
        .map(d => Object.assign(
            {},
            d,
            {
                columnEntityId: null,
                columnEntityKind: entity.CHANGE_INITIATIVE.key,
                entityFieldReference: d,
                columnName: entity.CHANGE_INITIATIVE.name,
                displayName: null
            }))
        .value();

    $: rowData = _
        .chain(fieldReferences)
        .filter(selectionFilter)
        .orderBy(d => d.entityFieldReference.displayName)
        .value();

    const columnDefs = [
        {field: "entityFieldReference.displayName", name: "Field", width: "30%"},
        {field: "entityFieldReference.description", name: "Description", width: "70%"},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select a change initiative field from the list below, you can filter the list using the search bar.
</div>
<br>
{#if _.isEmpty(rowData)}
    <NoData type="info">There are no more fields to add</NoData>
{:else }
    <Grid {columnDefs}
          {rowData}
          onSelectRow={onSelect}/>
{/if}