<script>

    import {onMount} from "svelte";
    import _ from "lodash";
    import {SlickGrid} from "slickgrid";

    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {baseColumns, doGridSearch, mkColumnDefs, mkGridData} from "./measurable-ratings-view-grid-utils";
    import DataExtractLink from "../../../common/svelte/DataExtractLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {measurableRatingStore} from "../../../svelte-stores/measurable-rating-store";
    import {selectedCategory, selectedMeasurable, showPrimaryOnly} from "./measurable-rating-view-store";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 1,
    };

    function initGrid(elem) {
        grid = new SlickGrid(elem, [], columns, options);
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
    }

    export let selectionOptions;

    let viewData = [];
    let columns = baseColumns;
    let viewCall;

    let elem = null;
    let grid;
    let searchStr = "";
    let viewParams;

    onMount(() => {
        viewData = [];
    });

    $: {
        viewParams = {
            idSelectionOptions: selectionOptions,
            parentMeasurableId: $selectedMeasurable?.id
        }
    }

    $: {
        if (!_.isEmpty($selectedCategory) && selectionOptions) {
            viewCall = measurableRatingStore.getViewByCategoryAndSelector($selectedCategory.id, selectionOptions, false);
        }
    }

    $: {
        const data  = doGridSearch(viewData, searchStr);
        if (grid) {
            grid.data = data;
            grid.invalidate();
        }
    }

    $: {
        if (!_.isEmpty(viewCall) && !_.isEmpty($viewCall?.data)) {
            const {applications, primaryAssessments, primaryRatings, measurableRatings, allocations, decommissions} = $viewCall.data;
            columns = mkColumnDefs(measurableRatings, primaryAssessments, primaryRatings, allocations, decommissions);
            const gridData = mkGridData(applications, measurableRatings, primaryAssessments, primaryRatings, allocations, decommissions, $showPrimaryOnly);
            viewData = _.filter(gridData, d => _.includes(d.parentIds, $selectedMeasurable?.id));
        }

        if (elem) {
            initGrid(elem);
        }
    }

</script>

{#if ($viewCall?.status === 'loading')}
    <LoadingPlaceholder>
        Loading...
    </LoadingPlaceholder>
{/if}

{#if $viewCall?.status === 'loaded'}
    {#if !_.isEmpty(viewData)}
        <SearchInput bind:value={searchStr}/>

        <div class="slick-container"
             style="width:100%;height:500px;"
             bind:this={elem}>
        </div>

        <div class="small help-block">
            Showing {viewData.length} ratings
        </div>
        <div class="small" style="display: inline-block">
            <DataExtractLink name="Export ratings"
                             filename={`${$selectedCategory.name} ratings`}
                             extractUrl={`measurable-rating-view/category-id/${$selectedCategory.id}`}
                             method="POST"
                             requestBody={viewParams}
                             styling="link"/>
        </div>
    {:else}
        <NoData>No ratings</NoData>
    {/if}
{/if}