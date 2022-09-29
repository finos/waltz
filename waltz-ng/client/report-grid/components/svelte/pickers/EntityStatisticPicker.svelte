<script>

    import _ from "lodash";
    import {entityStatisticStore} from "../../../../svelte-stores/entity-statistic-store";
    import EntityStatisticTreeSelector from "../../../../common/svelte/EntityStatisticTreeSelector.svelte";
    import {onDeselect} from "./DataTypePicker.svelte";

    export let onSelect = () => console.log("Selecting entity statistic");
    export let selectionFilter = () => true;
    export let subjectKindFilter = () => true;

    $: statDefinitionsCall = entityStatisticStore.findAllActiveDefinitions();
    $: statDefinitions = $statDefinitionsCall.data; // perhaps need a target kind col on stat definition?

    $: rowData = _
        .chain(statDefinitions)
        .map(d => Object.assign(
            {},
            d,
            {
                columnEntityId: d.id,
                columnEntityKind: d.kind,
                entityFieldReference: null,
                columnName: d.name,
                displayName: null
            }))
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        {field: "name", name: "Statistic Definition", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300},
    ];


    function handleSelection(evt) {
        const d = evt.detail;

        const payload = {
            columnEntityId: d.id,
            columnEntityKind: "ENTITY_STATISTIC",
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

    function statisticSelectionFilter(selectionFilter, statistic) {

        const payload = {
            columnEntityId: statistic.id,
            columnEntityKind: "ENTITY_STATISTIC",
            entityFieldReference: null,
            columnName: statistic.name,
            displayName: null
        };

        return selectionFilter(payload);
    }

</script>

<EntityStatisticTreeSelector multiSelect={true}
                             selectionFilter={(d) => statisticSelectionFilter(selectionFilter, d)}
                             on:select={handleSelection}/>