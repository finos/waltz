<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {entityFieldReferenceStore} from "../../../../svelte-stores/entity-field-reference-store";
    import {entity} from "../../../../common/services/enums/entity";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {mkReportGridEntityFieldReferenceColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting application field");
    export let selectionFilter = () => true;

    $: entityFieldReferenceCall = entityFieldReferenceStore.findAll();
    $: entityFieldReferences = $entityFieldReferenceCall.data;

    $: rowData = _
        .chain(entityFieldReferences)
        .filter(d => d.entityKind === "APPLICATION")
        .map(d => mkReportGridEntityFieldReferenceColumnRef(d, entity.APPLICATION))
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
    Select an application field from the list below, you can filter the list using the search bar.
</div>
<br>
{#if _.isEmpty(rowData)}
    <NoData type="info">There are no more fields to add</NoData>
{:else }
    <Grid {columnDefs}
          {rowData}
          onSelectRow={onSelect}/>
{/if}