<script>

    import Grid from "../Grid.svelte";
    import Icon from "../Icon.svelte";
    import _ from "lodash";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";
    import {measurableStore} from "../../../svelte-stores/measurables";
    import {mkSelectionOptions} from "../../selector-utils";
    import {buildHierarchies} from "../../hierarchy-utils";
    import MeasurableTreeSelector from "../MeasurableTreeSelector.svelte";

    export let onSelect = () => console.log("Selecting measurable");
    export let onDeselect = () => console.log("Deselecting measurable");
    export let selectionFilter = () => true;

    $: measurableCategoriesCall = measurableCategoryStore.findAll();
    $: categories = $measurableCategoriesCall.data;

    $: measurablesCall = selectedCategory && measurableStore.findMeasurablesBySelector(mkSelectionOptions(selectedCategory, "EXACT"));
    $: measurables = $measurablesCall?.data || [];

    let selectedCategory = null;
    let rowData;
    let selected = [];

    $: [rowData, selected] = _.partition(measurables, d => selectionFilter(d));

    $: selectedIds = _.map(selected, d => d.id);

    const categoryColumnDefs = [
        {field: "name", name: "Measurable Category", width: "30%"},
        {field: "description", name: "Description", width: "70%"},
    ];

    const columnDefs = [
        {field: "name", name: "Name", width: "30%"},
        { field: "description", name: "Description", width: "70%"},
    ];

    function selectCategory(d){
        selectedCategory = d;
    }

    function clearSelectedCategory(){
        selectedCategory = null;
    }

    $: hierarchy = { name:"root", hideNode: true, children: buildHierarchies(measurables, false)};

</script>


{#if selectedCategory}
    <div class="help-block small">
        <span>
            <Icon name="info-circle"/>Select a measurable from the list below, you can filter the list using the search bar or
            <button on:click={clearSelectedCategory}
                    class="btn-skinny">
                choose a different category
            </button>.
        </span>
    </div>
    <p>Measurables for category: <strong>{selectedCategory.name}</strong></p>
    <MeasurableTreeSelector {measurables}
                            {selected}
                            onSelect={onSelect}
                            onDeselect={onDeselect}/>
{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a category from the list below, you can filter the list using the search bar.
    </div>
    <br>
    <Grid columnDefs={categoryColumnDefs}
          rowData={categories}
          onSelectRow={selectCategory}/>
{/if}