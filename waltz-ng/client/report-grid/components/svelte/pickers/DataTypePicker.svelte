<script>

    import DataTypeTreeSelector from "../../../../common/svelte/DataTypeTreeSelector.svelte";

    export let onSelect = (dt) => console.log("Selecting dataType", {dt});
    export let onDeselect = (dt) => console.log("Deselecting dataType", {dt});
    export let selectionFilter = () => true;

    function handleSelection(evt) {
        const payload = mkDataTypeColumn(evt.detail);
        if (selectionFilter(payload)) {
            onSelect(payload);
        } else {
            onDeselect(payload);
        }
    }

    function mkDataTypeColumn(datatype) {
        return {
            kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
            columnEntityId: datatype.id,
            columnEntityKind: "DATA_TYPE",
            entityFieldReference: null,
            columnName: datatype.name,
            displayName: null
        };
    }

    function dataTypeSelectionFilter(selectionFilter, datatype) {
        const payload = mkDataTypeColumn(datatype);
        return selectionFilter(payload);
    }

</script>


<DataTypeTreeSelector multiSelect={true}
                      selectionFilter={(d) => dataTypeSelectionFilter(selectionFilter, d)}
                      on:select={handleSelection}/>