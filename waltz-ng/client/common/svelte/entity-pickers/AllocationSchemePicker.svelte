<script>

    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import _ from "lodash";
    import {allocationSchemeStore} from "../../../svelte-stores/allocation-scheme-store";

    export let onSelect = () => console.log("Selecting allocation scheme kind");
    export let selectionFilter = () => true;

    $: allocationSchemesCall = allocationSchemeStore.findAll();
    $: allocationSchemes = $allocationSchemesCall?.data;

    $: rowData = _
        .chain(allocationSchemes)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value()

    const columnDefs = [
        {field: "name", name: "Allocation Scheme", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300}
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select an allocation scheme from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>