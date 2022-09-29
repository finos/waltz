<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {buildHierarchies} from "../../../../common/hierarchy-utils";
    import MeasurableTreeSelector from "../../../../common/svelte/MeasurableTreeSelector.svelte";

    export let onSelect = () => console.log("Selecting measurable");
    export let onDeselect = () => console.log("Deselecting measurable");
    export let selectionFilter = () => true;

    $: measurableCategoriesCall = measurableCategoryStore.findAll();
    $: categories = $measurableCategoriesCall.data;

    $: measurablesCall = selectedCategory && measurableStore.findMeasurablesBySelector(mkSelectionOptions(selectedCategory, "EXACT"));
    $: measurables = $measurablesCall?.data || [];

    $: measurableColumns = _.map(measurables, d => Object.assign(
        {},
        d,
        {
            columnEntityId: selectedCategory.id,
            columnEntityKind: "MEASURABLE_CATEGORY",
            entityFieldReference: null,
            columnQualifierKind: "MEASURABLE",
            columnQualifierId: d.id,
            columnName: `${selectedCategory.name}/${d.name}`,
            displayName: null
        }));


    $: wholeCategory = {
        columnEntityId: selectedCategory?.id,
        columnEntityKind: "MEASURABLE_CATEGORY",
        entityFieldReference: null,
        columnQualifierKind: null,
        columnQualifierId: null,
        columnName: `${selectedCategory?.name}`,
        displayName: null
    }

    let selectedCategory = null;
    let rowData;
    let selected = [];

    $: [rowData, selected] = _.partition(measurableColumns, selectionFilter);

    const categoryColumnDefs = [
        {field: "name", name: "Measurable Category", width: "30%"},
        {field: "description", name: "Description", width: "70%"},
    ];

    const columnDefs = [
        {field: "name", name: "Name", width: "30%"},
        {field: "description", name: "Description", width: "70%"},
    ];

    function selectCategory(d) {
        selectedCategory = d;
    }

    function clearSelectedCategory() {
        selectedCategory = null;
    }

    function selectAllForCategory() {
        let notSelected = selectionFilter(wholeCategory);

        if (notSelected) {
            onSelect(wholeCategory);
        } else {
            onDeselect(wholeCategory);
        }

    }

    $: hierarchy = {name: "root", hideNode: true, children: buildHierarchies(measurables, false)};

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
    <MeasurableTreeSelector measurables={measurableColumns}
                            selected={selected}
                            {onSelect}
                            {onDeselect}/>
    <button class="btn btn-skinny"
            on:click={selectAllForCategory}>
        <Icon name={selectionFilter(wholeCategory) ? "square-o": "check-square-o"}/>
        Show all mappings to this category
    </button>

{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>
        Select a category from the list below, you can filter the list using the search bar.
    </div>
    <br>
    <Grid columnDefs={categoryColumnDefs}
          rowData={categories}
          onSelectRow={selectCategory}/>
{/if}