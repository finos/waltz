<script>

    import DataTypeTreeSelector from "../DataTypeTreeSelector.svelte";

    export let onSelect = (dt) => console.log("Selecting dataType", {dt});
    export let onDeselect = (dt) => console.log("Deselecting dataType", {dt});
    export let selectionFilter = () => true;

    function handleSelection(evt) {
        const datatype = evt.detail;
        if (selectionFilter(datatype)) {
            onSelect(datatype);
        } else {
            onDeselect(datatype);
        }
    }

</script>


<DataTypeTreeSelector multiSelect={true}
                      selectionFilter={selectionFilter}
                      on:select={handleSelection}/>