<script>

    import _ from "lodash";
    import {entityStatisticStore} from "../../../../svelte-stores/entity-statistic-store";
    import EntityStatisticTreeSelector from "../../../../common/svelte/EntityStatisticTreeSelector.svelte";
    import {mkReportGridFixedColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting entity statistic");
    export let onDeselect = () => console.log("Deselecting entity statistic");
    export let selectionFilter = () => true;

    $: statDefinitionsCall = entityStatisticStore.findAllActiveDefinitions();
    $: statDefinitions = $statDefinitionsCall.data; // perhaps need a target kind col on stat definition?

    $: rowData = _
        .chain(statDefinitions)
        .filter(d => selectionFilter(mkReportGridFixedColumnRef(d)))
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        {field: "name", name: "Statistic Definition", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300},
    ];

    function handleSelection(evt) {
        const payload = mkReportGridFixedColumnRef(evt.detail);

        if (selectionFilter(payload)) {
            onSelect(payload);
        } else {
            onDeselect(payload);
        }
    }

    function statisticSelectionFilter(selectionFilter, statistic) {
        const payload = mkReportGridFixedColumnRef(statistic)
        return selectionFilter(payload);
    }

</script>

<EntityStatisticTreeSelector multiSelect={true}
                             selectionFilter={(d) => statisticSelectionFilter(selectionFilter, d)}
                             on:select={handleSelection}/>