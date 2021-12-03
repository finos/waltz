<script>

    import {involvementKindStore} from "../../../../svelte-stores/involvement-kind-store";
    import Grid from "../../../../playpen/1/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: involvementKindCall = involvementKindStore.findAll();
    $: involvementKinds = $involvementKindCall.data;

    $: rowData = _.filter(involvementKinds, selectionFilter)

    const columnDefs = [
        { field: "name", name: "Involvement Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select an involvement from the list below, you can filter the list using the search bar.
</div>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>