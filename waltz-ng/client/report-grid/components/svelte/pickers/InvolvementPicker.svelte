<script>

    import {involvementKindStore} from "../../../../svelte-stores/involvement-kind-store";
    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: involvementKindCall = involvementKindStore.findAll();
    $: involvementKinds = $involvementKindCall.data;

    $: rowData = _
        .chain(involvementKinds)
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
        { field: "name", name: "Involvement Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%", maxLength: 300},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select an involvement from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>