<script>

    import _ from "lodash";
    import {entityStatisticStore} from "../../../svelte-stores/entity-statistic-store";
    import EntityStatisticTreeSelector from "../EntityStatisticTreeSelector.svelte";

    export let onSelect = () => console.log("Selecting entity statistic");
    export let onDeselect = () => console.log("Deselecting entity statistic");
    export let selectionFilter = () => true;

    $: statDefinitionsCall = entityStatisticStore.findAllActiveDefinitions();
    $: statDefinitions = $statDefinitionsCall.data; // perhaps need a target kind col on stat definition?

    $: rowData = _
        .chain(statDefinitions)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        {field: "name", name: "Statistic Definition", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300},
    ];

    function handleSelection(evt) {
        const stat = evt.detail;

        if (selectionFilter(stat)) {
            onSelect(stat);
        } else {
            onDeselect(stat);
        }
    }

</script>

<EntityStatisticTreeSelector multiSelect={true}
                             selectionFilter={selectionFilter}
                             on:select={handleSelection}/>