<script>

    import {involvementKindStore} from "../../../../svelte-stores/involvement-kind-store";
    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {mkReportGridFixedColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: involvementKindCall = involvementKindStore.findAll();
    $: involvementKinds = $involvementKindCall.data;

    $: rowData = _
        .chain(involvementKinds)
        .filter(d => selectionFilter(mkReportGridFixedColumnRef(d)))
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
      onSelectRow={d => onSelect(mkReportGridFixedColumnRef(d))}/>