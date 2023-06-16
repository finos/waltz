<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import _ from "lodash";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true
    export let subjectKindFilter = () => true;

    $: involvementKindCall = involvementKindStore.findAll();
    $: involvementKinds = _.filter($involvementKindCall.data, d => subjectKindFilter(d.subjectKind));

    $: rowData = _
        .chain(involvementKinds)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        { field: "name", name: "Involvement Kind", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300}
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select an involvement from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>