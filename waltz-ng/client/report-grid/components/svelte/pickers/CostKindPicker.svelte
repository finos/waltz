<script>

    import Grid from "../../../../playpen/1/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {costKindStore} from "../../../../svelte-stores/cost-kind-store";
    import _ from "lodash";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: costKindCall = costKindStore.findAll();
    $: costKinds = $costKindCall.data;

    $: rowData = _.filter(costKinds, selectionFilter)

    const columnDefs = [
        { field: "name", name: "Cost Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select a cost kind from the list below, you can filter the list using the search bar.
</div>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>