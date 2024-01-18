<script>

    import {SlickGrid} from "slickgrid";
    import {onMount} from "svelte";
    import _ from "lodash";

    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {applicationStore} from "../../../svelte-stores/application-store";
    import {orgUnitStore} from "../../../svelte-stores/org-unit-store";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {baseColumns, doGridSearch, mkColumnDefs, mkGridData} from "./apps-view-grid-utils";
    import DataExtractLink from "../../../common/svelte/DataExtractLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 1
    };

    function initGrid(elem) {
        grid = new SlickGrid(elem, [], columns, options);
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
    }

    export let selectorOptions;

    let viewData = [];
    let orgUnitsById = {};
    let columns = baseColumns;

    let appViewCall = null;
    let orgUnitCall = orgUnitStore.loadAll();

    let elem = null;
    let grid;
    let searchStr = "";

    onMount(() => {
        viewData = [];
        appViewCall = applicationStore.getViewBySelector(selectorOptions)
    });

    $: orgUnitsById = _.keyBy($orgUnitCall.data, d => d.id);

    $: {
        const data  = doGridSearch(viewData, searchStr);
        if (grid) {
            grid.data = data;
            grid.invalidate();
        }
    }

    $: {
        if ($appViewCall?.data) {
            const {applications, primaryAssessments, primaryRatings} = $appViewCall.data;
            columns = mkColumnDefs(primaryAssessments, primaryRatings);
            viewData = mkGridData(applications, primaryAssessments, primaryRatings, orgUnitsById);
        }

        if (elem && !_.isEmpty(viewData)) {
            initGrid(elem);
        }
    }
</script>

{#if !_.isEmpty(viewData)}
    <SearchInput bind:value={searchStr}/>

    <div class="slick-container"
         style="width:100%;height:500px;"
         bind:this={elem}>
    </div>

    <div class="small help-block">
        Showing {viewData.length} applications
    </div>
    <div class="small" style="display: inline-block">
        <DataExtractLink name="Export Apps"
                         filename="applications"
                         extractUrl="application/by-selector"
                         method="POST"
                         requestBody={selectorOptions}
                         styling="link"/>
    </div>
{:else}
    <NoData>No applications</NoData>
{/if}
