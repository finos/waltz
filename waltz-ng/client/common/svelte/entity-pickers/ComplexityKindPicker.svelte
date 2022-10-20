<script>

    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import _ from "lodash";
    import {complexityKindStore} from "../../../svelte-stores/complexity-kind-store";

    export let onSelect = () => console.log("Selecting complexity kind");
    export let selectionFilter = () => true;

    $: complexityKindsCall = complexityKindStore.findAll();
    $: complexityKinds = $complexityKindsCall?.data;

    $: rowData = _
        .chain(complexityKinds)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value()

    const columnDefs = [
        {field: "name", name: "Complexity Kind", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300}
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select an complexity kind from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>