<script>

    import {involvementKindStore} from "../../svelte-stores/involvement-kind-store";
    import Grid from "./Grid.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {assessmentDefinitionStore} from "../../svelte-stores/assessment-definition";
    import {measurableCategoryStore} from "../../svelte-stores/measurable-category-store";
    import {measurableStore} from "../../svelte-stores/measurables";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import {mkRef} from "../../common/entity-utils";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    $: measurableCategoriesCall = measurableCategoryStore.findAll();
    $: categories = $measurableCategoriesCall.data;

    $: measurablesCall = selectedCategory && measurableStore.findMeasurablesBySelector(mkSelectionOptions(selectedCategory));
    $: measurables = $measurablesCall?.data || [];

    let selectedCategory = null;

    $: rowData = _.filter(measurables, selectionFilter)

    const categoryColumnDefs = [
        { field: "name", name: "Measurable Category", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

    const columnDefs = [
        { field: "name", name: "Name", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

    function selectCategory(d){
        selectedCategory = d;
    }

    $: console.log({categories, measurables})

</script>


{#if selectedCategory}
    <div class="help-block small"><Icon name="info-circle"/>Select a measurable from the list below, you can filter the list using the search bar.</div>
    <Grid {columnDefs}
          {rowData}
          onSelectRow={onSelect}/>
{:else}
    <div class="help-block small"><Icon name="info-circle"/>Select a measurable from the list below, you can filter the list using the search bar.</div>
    <Grid columnDefs={categoryColumnDefs}
          rowData={categories}
          onSelectRow={selectCategory}/>
{/if}