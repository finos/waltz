<script>

    import Grid from "../../../../playpen/1/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {assessmentDefinitionStore} from "../../../../svelte-stores/assessment-definition";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: assessmentDefinitionsCall = assessmentDefinitionStore.loadAll();
    $: assessmentDefintions = $assessmentDefinitionsCall.data;

    $: rowData = _.filter(assessmentDefintions, selectionFilter)

    const columnDefs = [
        { field: "name", name: "Involvement Kind", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>Select an assessment definition from the list below, you can filter the list using the search bar.
</div>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>