<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {costKindStore} from "../../../../svelte-stores/cost-kind-store";
    import _ from "lodash";
    import {mkReportGridFixedColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting cost kind");
    export let selectionFilter = () => true;

    $: costKindCall = costKindStore.findAll();
    $: costKinds = $costKindCall.data;

    $: rowData = _
        .chain(costKinds)
        .filter(d => selectionFilter(mkReportGridFixedColumnRef(d)))
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        { field: "name", name: "Cost Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select a cost kind from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={d => onSelect(mkReportGridFixedColumnRef(d))}/>