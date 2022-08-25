<script>

    import _ from "lodash";
    import DataTypeTreeSelector from "../../../../common/svelte/DataTypeTreeSelector.svelte";

    export let onSelect = (dt) => console.log("Selecting dataType", {dt});
    export let onDeselect = (dt) => console.log("Deselecting dataType", {dt});
    export let selectionFilter = () => true;

    function handleSelection(evt) {
        const d = evt.detail;

        const payload = {
                columnEntityId: d.id,
                columnEntityKind: "DATA_TYPE",
                entityFieldReference: null,
                columnName: d.name,
            displayName: null
        };

        if (selectionFilter(payload)) {
            onSelect(payload);
        } else {
            onDeselect(payload);
        }
    }

    function dataTypeSelectionFilter(selectionFilter, datatype) {

        const payload = {
            columnEntityId: datatype.id,
            columnEntityKind: "DATA_TYPE",
            entityFieldReference: null,
            columnName: datatype.name,
            displayName: null
        };

        return selectionFilter(payload);
    }

</script>


<DataTypeTreeSelector multiSelect={true}
                      selectionFilter={(d) => dataTypeSelectionFilter(selectionFilter, d)}
                      on:select={handleSelection}/>