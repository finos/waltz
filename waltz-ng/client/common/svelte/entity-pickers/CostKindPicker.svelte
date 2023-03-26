<script>

    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import {costKindStore} from "../../../svelte-stores/cost-kind-store";
    import _ from "lodash";

    const columnDefs = [
        { field: "name", name: "Cost Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

    export let onSelect = () => console.log("Selecting cost kind");
    export let selectionFilter = () => true;

    let rowData = [];

    $: costKindCall = costKindStore.findAll();
    $: costKinds = $costKindCall.data;
    $: rowData = _
        .chain(costKinds)
        .map(d => d.costKind)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();
</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select a cost kind from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>