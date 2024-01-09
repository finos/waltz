<script>

    import {buildHierarchies} from "../../../../common/hierarchy-utils";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {mkReportGridEntityFieldReferenceColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import MeasurableTreeSelector from "../../../../common/svelte/MeasurableTreeSelector.svelte";
    import Grid from "../../../../common/svelte/Grid.svelte";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {entityFieldReferenceStore} from "../../../../svelte-stores/entity-field-reference-store";
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let onSelect = () => console.log("Selecting measurable");
    export let onDeselect = () => console.log("Deselecting measurable");
    export let selectionFilter = () => true;

    $: measurableCategoriesCall = measurableCategoryStore.findAll();
    $: categories = $measurableCategoriesCall.data;

    $: measurablesCall = selectedCategory && measurableStore.findMeasurablesBySelector(mkSelectionOptions(selectedCategory, "EXACT"));
    $: measurables = $measurablesCall?.data || [];

    const Modes = {
        LIST: "LIST",
        PRIMARY_FIELD: "PRIMARY_FIELD",
        MEASURABLE: "MEASURABLE"
    }

    let selectedCategory = null;
    let rowData;
    let selected = [];
    let showPrimary = false;
    let activeMode = Modes.LIST

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

    function showPrimaryFieldPicker(){
        activeMode = Modes.PRIMARY_FIELD;
    }

    function showMeasurablePicker(){
        activeMode = Modes.MEASURABLE;
    }

    $: entityFieldReferenceCall = entityFieldReferenceStore.findAll();
    $: entityFieldReferences = $entityFieldReferenceCall.data;

    $: rowData = _
        .chain(entityFieldReferences)
        .filter(d => d.entityKind === "MEASURABLE")
        .map(d => mkReportGridEntityFieldReferenceColumnRef(d, entity.MEASURABLE, selectedCategory, `Primary Rating ${d.displayName}`))
        .filter(selectionFilter)
        .orderBy(d => d.entityFieldReference.displayName)
        .value();

    const primaryFieldColumnDefs = [
        {field: "entityFieldReference.displayName", name: "Field", width: "30%"},
        {field: "entityFieldReference.description", name: "Description", width: "70%"},
    ];

    $: hierarchy = { name:"root", hideNode: true, children: buildHierarchies(measurables, false)};

</script>


{#if selectedCategory}
    {#if activeMode === Modes.LIST}
        <div class="help-block small">
            <span>
                <Icon name="info-circle"/>Adding viewpoint information for {selectedCategory.name}, or
                <button on:click={clearSelectedCategory}
                        class="btn-skinny">
                    choose a different category
                </button>.
            </span>
        </div>
        {#if selectedCategory.allowPrimaryRatings}
            <div style="padding-top: 1em;">
                <button class="btn btn-skinny"
                        on:click={showPrimaryFieldPicker}>
                    <Icon name="star"/> Add primary rating information
                </button>
                <div class="text-muted">Add information about the primary rating for this taxonomy e.g. the name of the item or its external identifier</div>
            </div>
        {/if}
        <div style="padding-top: 1em;">
            <button class="btn btn-skinny"
                    on:click={showMeasurablePicker}>
                <Icon name="puzzle-piece"/> Add viewpoint ratings
            </button>
            <div class="text-muted">Add a specific taxonomy item to the grid, the rating value will be shown in the cell. You can configure how the ratings rollup in the column editor.</div>
        </div>
    {:else if activeMode === Modes.MEASURABLE}
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
    {:else if activeMode === Modes.PRIMARY_FIELD}
        <div class="help-block small">
            <Icon name="info-circle"/>
            Select a measurable field from the list below, you can filter the list using the search bar.
        </div>
        <br>
        {#if _.isEmpty(rowData)}
            <NoData type="info">There are no more fields to add</NoData>
        {:else }
            <Grid columnDefs={primaryFieldColumnDefs}
                  {rowData}
                  onSelectRow={onSelect}/>
        {/if}
    {/if}
{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a category from the list below, you can filter the list using the search bar.
    </div>
    <br>
    <Grid columnDefs={categoryColumnDefs}
          rowData={categories}
          onSelectRow={selectCategory}/>
{/if}