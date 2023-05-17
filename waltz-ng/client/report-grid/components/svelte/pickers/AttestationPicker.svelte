<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";

    export let onSelect = () => console.log("Selecting attestation kind");
    export let selectionFilter = () => true;

    const baseAttestationKinds = [
        {
            kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
            name: "Logical Flow",
            description: "Latest logical flow attestation",
            columnEntityId: null,
            columnEntityKind: entity.ATTESTATION.key,
            entityFieldReference: null,
            columnName: "Logical Flow Attestation",
            columnQualifierKind: entity.LOGICAL_DATA_FLOW.key,
            columnQualifierId: null,
            displayName: null
        }, {
            kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
            name: "Physical Flow",
            description: "Latest physical flow attestation",
            columnEntityId: null,
            columnEntityKind: entity.ATTESTATION.key,
            entityFieldReference: null,
            columnName: "Physical Flow Attestation",
            columnQualifierKind: entity.PHYSICAL_FLOW.key,
            columnQualifierId: null,
            displayName: null
        }
    ];

    const columnDefs = [
        {field: "name", name: "Attestation Kind", width: "40%"},
        {field: "description", name: "Description", width: "60%", maxLength: 300},
    ];

    let measurableCategoriesCall = null;

    $: measurableCategoriesCall = measurableCategoryStore.findAll();

    $: rowData = _
        .chain($measurableCategoriesCall.data)
        .map(d => ({
            kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
            columnEntityKind: "ATTESTATION",
            columnEntityId: null,
            columnQualifierKind: "MEASURABLE_CATEGORY",
            columnQualifierId: d.id,
            columnName: `${d.name} Attestation`,
            description: `Latest attestation for category: ${d.name}`,
            name: d.name
        }))
        .concat(baseAttestationKinds)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();


    function selectAttestation(d) {

        const col = {

            kind: d.kind,
            columnEntityKind: d.columnEntityKind,
            columnEntityId: d.columnEntityId,
            columnQualifierKind: d.columnQualifierKind,
            columnQualifierId: d.columnQualifierId,
            columnName: d.columnName,
        }

        return onSelect(col);
    }

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select a an attestation kind from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid columnDefs={columnDefs}
      rowData={rowData}
      onSelectRow={selectAttestation}/>
