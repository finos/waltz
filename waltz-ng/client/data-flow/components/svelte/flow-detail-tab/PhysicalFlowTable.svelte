<script>

    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import _ from "lodash";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";
    import {selectedPhysicalFlow, selectedLogicalFlow} from "./flow-details-store";
    import {
        mkPhysicalFlowTableColumns,
        showDataTypeTooltip
    } from "./flow-detail-utils";
    import {SlickGrid, SlickRowSelectionModel} from "slickgrid";
    import {mkSortFn} from "../../../../common/slick-grid-utils";
    import {entity as EntityKinds} from "../../../../common/services/enums/entity";


    export let physicalFlows = [];
    export let flowClassifications = [];
    export let assessmentDefinitions = [];


    function initGrid(elem, nestedEnums) {
        let columns = mkPhysicalFlowTableColumns(defs, nestedEnums);

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
                showDataTypeTooltip(cellElem, rowData.dataTypesForSpecification);
            }
        });
        grid.onClick.subscribe((a,b) => selectPhysicalFlow(flowList[b.row]))

        grid.data = flowList;
        grid.invalidate()
    }


    function selectPhysicalFlow(flow) {
        if ($selectedPhysicalFlow === flow) {
            $selectedPhysicalFlow = null;
            $selectedLogicalFlow = null;
        } else {
            $selectedPhysicalFlow = flow;
            $selectedLogicalFlow = flow;
        }
    }

    const gridOptions = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 3
    };

    let qry;
    let grid;
    let elem = null;

    let visibleFlows;
    let enumsCall = enumValueStore.load();
    let nestedEnums = null;

    $: nestedEnums = nestEnums($enumsCall.data);

    $: visibleFlows = _.filter(physicalFlows, d => d.visible);

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
                (f) => _.get(f.physicalFlow, ["name"], ""),
                (f) => _.get(f.physicalFlow, ["externalId"], ""),
                (f) => _.get(f.physicalFlow, ["frequency"], ""),
                (f) => _.get(f.physicalFlow, ["transport"], ""),
                (f) => _.get(f.physicalFlow, ["criticality"], ""),
                (f) => _.get(f.specification, ["name"], ""),
                (f) => _.chain(f.physicalFlowRatingsByDefId).values().flatten().map(d => d.name).join(" ").value(),
                (f) => _.chain(f.physicalSpecRatingsByDefId).values().flatten().map(d => d.name).join(" ").value(),
                (f) => _.chain(f.dataTypesForSpecification).map(d => _.get(d, ["decoratorEntity", "name"])).join(" ").value()
            ]);

    $: defs = _.filter(
        assessmentDefinitions,
        d => d.entityKind === EntityKinds.PHYSICAL_FLOW.key ||
            d.entityKind === EntityKinds.PHYSICAL_SPECIFICATION.key);

    $: {
        if (elem && !_.isEmpty(flowList) && nestedEnums) {
            initGrid(elem, nestedEnums);
        }
    }

    $: {
        if (grid) {
            grid.setSelectedRows([_.indexOf(flowList, $selectedPhysicalFlow)]);
        }
    }

</script>


<h4>
    Physical Flows
    <span class="small">
        (
        {#if _.size(flowList) !== _.size(physicalFlows)}
            {_.size(flowList)} /
        {/if}
        {_.size(physicalFlows)}
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
