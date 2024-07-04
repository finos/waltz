<script>
    import _ from "lodash";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import {SlickGrid, SlickRowSelectionModel} from "slickgrid";
    import {mkSortFn} from "../../../common/slick-grid-utils";
    import {mkGridData} from "./flow-classification-rules-table-utils";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {createEventDispatcher} from "svelte"

    const dispatcher = createEventDispatcher();
    const gridOptions = {
        enableCellNavigation: false,
        enableColumnReorder: false,
        frozenColumn: 1
    };

    export let rulesView;

    let elem = null;
    let grid;
    let gridData = null;
    let displayData = [];
    let qry= "";
    let gridInitLatch = false;

    function searchRows(rows = [], qry) {
        return termSearch(
            rows,
            qry,
            [
                (d) => _.get(d, ["classification", "name"], ""),
                (d) => {
                    const direction = _.get(d, ["direction"], "")
                    switch (direction) {
                        case "INBOUND":
                            return "consumer";
                        case "OUTBOUND":
                            return "producer";
                        default:
                            return "";
                    }
                },
                (d) => _
                    .chain(d.assessmentRatingsByDefinitionId)
                    .values()
                    .flatten()
                    .map(v => v.name)
                    .join(" ")
                    .value(),
                (d) => _.get(d, ["dataType", "name"], ""),
                (d) => _.get(d, ["subjectReference", "name"], ""),
                (d) => _.get(d, ["vantagePointReference", "name"], "")
            ]);
    }

    function initGrid(elem) {
        if (gridInitLatch) {
            grid.data = displayData.rows;
            grid.invalidate();
        } else {
            let columns = displayData.columns;
            grid = new SlickGrid(elem, [], columns, gridOptions);
            grid.setSelectionModel(new SlickRowSelectionModel());
            grid.onSort.subscribe((e, args) => {
                const sortCol = args.sortCol;
                grid.data.sort(mkSortFn(sortCol, args.sortAsc));
                grid.invalidate();
            });
            grid.onClick.subscribe((a,b) => {
                const rowData = grid.data[b.row];
                dispatcher("select", rowData);
            });

            grid.data = displayData.rows;
            grid.invalidate()
            gridInitLatch = true;
        }
    }

    $: {
        if (elem && !_.isNil(displayData)) {
            initGrid(elem);
        }
    }

    $: gridData = mkGridData(rulesView);

    $: {
        if (! _.isNil(gridData)) {
            const rows = _.isEmpty(qry)
                ? gridData.rows
                : searchRows(gridData.rows, qry);

            displayData = Object.assign({}, gridData, {rows});
        }
    }
</script>


{#if _.isNil(gridData) && ! gridInitLatch}
    <LoadingPlaceholder>
        Loading flow classification rules
    </LoadingPlaceholder>
{:else}

    <div>
        <SearchInput bind:value={qry}/>
    </div>
    <div class="slick-container"
         style="width:100%;height:500px;"
         bind:this={elem}>
    </div>
    <div class="help-block">
        Showing {displayData?.rows.length} / {gridData?.rows.length} rules
    </div>
{/if}
