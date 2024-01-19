<script>

    import {onMount} from "svelte";
    import _ from "lodash";
    import {SlickGrid} from "slickgrid";

    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {
        baseColumns,
        doGridSearch,
        mkUnmappedColumnDefs,
        mkUnmappedGridData
    } from "./measurable-ratings-view-grid-utils";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {measurableRatingStore} from "../../../svelte-stores/measurable-rating-store";
    import {selectedCategory} from "./measurable-rating-view-store";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        forceFitColumns: true
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
        if (!_.isEmpty($selectedCategory) && selectionOptions) {
            viewCall = measurableRatingStore.getViewByCategoryAndSelector($selectedCategory.id, selectionOptions, true)
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
            const {applications, measurableRatings} = $viewCall.data;
            columns = mkUnmappedColumnDefs();
            viewData = mkUnmappedGridData(applications, measurableRatings, $selectedCategory.id);
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
            Showing {viewData.length} applications
        </div>
    {:else}
        <NoData>No unmapped applications</NoData>
    {/if}
{/if}