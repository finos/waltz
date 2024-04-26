<script>
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import _ from "lodash";
    import {filters, selectedLogicalFlow, selectedPhysicalFlow, updateFilters} from "./flow-details-store";
    import {SlickGrid, SlickRowSelectionModel} from "slickgrid";
    import {mkSortFn} from "../../../../common/slick-grid-utils";
    import {mkLogicalFlowTableColumns, showDataTypeTooltip
    } from "./flow-detail-utils";

    export let logicalFlows = [];
    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    const gridOptions = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 4
    };

    let elem = null;
    let grid;
    let qry;
    let selectionLatchOpen = true;
    let defs = [];
    let flowClassificationsByCode = {};

    function isSameFlow(a, b) {
        const aId = _.get(a, ["logicalFlow", "id"]);
        const bId = _.get(b, ["logicalFlow", "id"]);
        return aId === bId;
    }

    function selectLogicalFlow(flow) {

        const addFilter = () => {
            const lfId = flow.logicalFlow.id;
            updateFilters(
                "SELECTED_LOGICAL",
                {
                    id: "SELECTED_LOGICAL",
                    test: (r) => r.logicalFlow.id === lfId
                });
        };

        const removeFilter = () => {
            $filters = _.reject($filters, d => d.id === "SELECTED_LOGICAL");
        };

        if (isSameFlow($selectedLogicalFlow, flow)) {
            if ($selectedPhysicalFlow && selectionLatchOpen) {
                selectionLatchOpen = false;
                addFilter();
            } else {
                $selectedLogicalFlow = null;
                $selectedPhysicalFlow = null;
                selectionLatchOpen = true;
                removeFilter();
            }
        } else {
            selectionLatchOpen = true;
            $selectedLogicalFlow = flow;
            $selectedPhysicalFlow = null;
            addFilter();
        }
    }

    function initGrid(elem) {
        let columns = mkLogicalFlowTableColumns(defs);
        grid = new SlickGrid(elem, [], columns, gridOptions);
        grid.setSelectionModel(new SlickRowSelectionModel());
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
        grid.onMouseEnter.subscribe(function(e, args) {
            const cell = grid.getCellFromEvent(e);
            if (! cell) return;

            const columnDef = columns[cell.cell];
            if (columnDef.id === 'data_types') {
                const rowData = flowList[cell.row];
                const cellElem = e.target;
                showDataTypeTooltip(cellElem, rowData.dataTypesForLogicalFlow, flowClassificationsByCode);
            }
        });
        grid.onClick.subscribe((a,b) => selectLogicalFlow(flowList[b.row]))

        grid.data = flowList;
        grid.invalidate()
    }

    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);

    $: visibleFlows = _.filter(logicalFlows, d => d.visible);

    $: flowList = _.isEmpty(qry)
        ? visibleFlows
        : termSearch(
            visibleFlows,
            qry,
            [
                (f) => _.get(f.logicalFlow.source, ["name"], ""),
                (f) => _.get(f.logicalFlow.source, ["externalId"], ""),
                (f) => _.get(f.logicalFlow.target, ["name"], ""),
                (f) => _.get(f.logicalFlow.target, ["externalId"], ""),
                (f) => _.chain(f.logicalFlowRatingsByDefId).values().flatten().map(d => d.name).join(" ").value(),
                (f) => _.chain(f.dataTypesForLogicalFlow).map(d => _.get(d, ["decoratorEntity", "name"])).join(" ").value()
            ]);

    $: defs = _.filter(
        assessmentDefinitions,
        d => d.entityKind === 'LOGICAL_DATA_FLOW');


    $: {
        if (elem && !_.isNil(flowList)) {
            initGrid(elem);
        }
    }

    $: {
        if (grid) {
            if (flowList && $selectedLogicalFlow) {
                const rowIdx = _.chain(flowList).map(f => f.logicalFlow).indexOf($selectedLogicalFlow.logicalFlow).value();
                grid.setSelectedRows([rowIdx]);
            } else {
                grid.setSelectedRows([]);
            }
        }
    }

</script>

<h4>
    Logical Flows
    <span class="small">
        (
        {#if _.size(flowList) !== _.size(logicalFlows)}
            {_.size(flowList)} /
        {/if}
        {_.size(logicalFlows)}
        )
    </span>
</h4>

<div>
    <SearchInput bind:value={qry}/>
</div>

<div class="slick-container"
     style="width:100%;height:500px;"
     bind:this={elem}>
</div>

