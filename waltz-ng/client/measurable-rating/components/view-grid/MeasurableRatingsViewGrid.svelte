<script>

    import {SlickGrid} from "slickgrid";
    import {onMount} from "svelte";
    import _ from "lodash";

    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {baseColumns, doGridSearch, mkColumnDefs, mkGridData} from "./measurable-ratings-view-grid-utils";
    import DataExtractLink from "../../../common/svelte/DataExtractLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {measurableRatingStore} from "../../../svelte-stores/measurable-rating-store";
    import {selectedMeasurable} from "./measurable-rating-view-store";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 0
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

    onMount(() => {
        viewData = [];
    });

    $: {
        if (!_.isEmpty($selectedMeasurable) && selectionOptions) {
            viewCall = measurableRatingStore.getViewByCategoryAndSelector($selectedMeasurable.categoryId, selectionOptions, true)
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
            console.log({data: $viewCall?.data});
            const {applications, primaryAssessments, primaryRatings, measurableRatings, allocations, decommissions} = $viewCall.data;
            columns = mkColumnDefs(measurableRatings, primaryAssessments, primaryRatings, allocations, decommissions);
            viewData = mkGridData(applications, measurableRatings, primaryAssessments, primaryRatings, allocations, decommissions);
            console.log({columns, viewData});
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
        <DataExtractLink name="Export Apps"
                         filename="applications"
                         extractUrl={`measurable-rating-view/category/${selectedMeasurable.categoryId}/selector`}
                         method="POST"
                         requestBody={selectionOptions}
                         styling="link"/>
    </div>
{:else}
    <NoData>No ratings</NoData>
{/if}

<style type="text/scss">
    @import "slickgrid/dist/styles/css/slick-alpine-theme.css";
</style>