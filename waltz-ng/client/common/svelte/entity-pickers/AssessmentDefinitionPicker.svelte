<script>

    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import _ from "lodash";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition";

    export let onSelect = () => console.log("Selecting assessment definition");
    export let selectionFilter = () => true;
    export let subjectKindFilter = () => true;

    $: assessmentDefinitionsCall = assessmentDefinitionStore.loadAll();
    $: assessmentDefintions = _.filter($assessmentDefinitionsCall.data, d => subjectKindFilter(d.entityKind));

    $: rowData = _
        .chain(assessmentDefintions)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();

    const columnDefs = [
        {field: "name", name: "Assessment Definition", width: "30%"},
        {field: "description", name: "Description", width: "70%", maxLength: 300},
    ];

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select an assessment definition from the list below, you can filter the list using the search bar.
</div>
<br>
<Grid {columnDefs}
      {rowData}
      onSelectRow={onSelect}/>