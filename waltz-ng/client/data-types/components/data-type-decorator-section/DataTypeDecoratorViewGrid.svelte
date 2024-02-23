<script>

    import NoData from "../../../common/svelte/NoData.svelte";
    import _ from "lodash";
    import {termSearch} from "../../../common";
    import {enrichedDecorators, filters, selectedDecorator, viewData} from "./data-type-decorator-section-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {SlickGrid, SlickRowSelectionModel} from "slickgrid";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {mkColumns} from "./data-type-decorator-view-grid-utils";
    import FlowDecoratorFilters from "./filters/FlowDecoratorFilters.svelte";

    let tableData = [];
    let filteredData = [];
    let qry;
    let assessmentFilters = []
    let elem = null;
    let grid;
    let columns = [];

    const options = {
        enableColumnReorder: false,
        enableCellNavigation: true,
        enableRowSelection: true,
        multiSelect: false
    };

    function initGrid(elem) {
        grid = new SlickGrid(elem, [], columns, options);
        grid.setSelectionModel(new SlickRowSelectionModel());
        grid.onSelectedRowsChanged.subscribe((e, args, d, f) => {
            const rowIdx = _.first(args.rows);
            $selectedDecorator = grid.getDataItem(rowIdx);
        })
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
    }

    $: {
        const data  = visibleRows;
        if (grid) {
            grid.data = data;
            grid.invalidate();
        }
    }

    function selectDecorator(decorator) {
        if (!_.isEmpty($selectedDecorator) && _.isEqual($selectedDecorator, decorator)) {
            $selectedDecorator = null;
        } else {
            $selectedDecorator = decorator;
        }
    }


    $:{
        if ($enrichedDecorators && $viewData) {

            if (elem) {
                initGrid(elem);
            }

            tableData = filterFlowDecorators($enrichedDecorators, $filters);
            columns = mkColumns($viewData.primaryAssessments.assessmentDefinitions);
        }
    }

    function filterFlowDecorators(flowDecorators = [], filters = []) {
        return _
            .chain(flowDecorators)
            .map(d => Object.assign(d, {visible: _.every(filters, f => f.test(d))}))
            .value();
    }

    $: filteredData = _.filter(tableData, d => d.visible);

    $: visibleRows = _.isEmpty(qry)
        ? filteredData
        : termSearch(filteredData, qry, ["decoratorEntity.name", "rating"]);


</script>


<div class="decorator-detail-table">
    {#if !_.isEmpty(tableData)}

        <FlowDecoratorFilters assessmentsView={$viewData.primaryAssessments}
                              classifications={$viewData.classifications}/>

        <div>
            <SearchInput bind:value={qry}/>
        </div>

        <div class="slick-container"
             style="width:100%; height:500px;"
             bind:this={elem}>
        </div>

    {:else}
        <NoData>There are no data type decorators associated to this flow</NoData>
    {/if}
</div>
