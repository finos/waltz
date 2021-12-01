<script>
    import NoData from "../../../common/svelte/NoData.svelte";
    import _ from "lodash";
    import {reportGridStore} from "../../../svelte-stores/report-grid-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ReportGridEditor from "./ReportGridEditor.svelte";
    import {toUpperSnakeCase} from "../../../common/string-utils";
    import toasts from "../../../svelte-stores/toast-store";

    export let onGridSelect = (d) => console.log("selecting grid", d);
    export let onCancel = (d) => console.log("cancelling");

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let activeMode = Modes.VIEW

    let grids = [];
    let selectedGridId = null;
    let selectedGrid = null;

    $: reportGridCall = reportGridStore.findAll();
    $: grids = $reportGridCall.data;

    function onSelect(grid) {
        selectedGridId = grid.id;
        onGridSelect(grid);
    }

    function createReportGrid() {
        selectedGrid = {
            name: null,
            description: null,
            externalId: null
        }
        activeMode = Modes.EDIT;
    }

    function editReportGrid(grid) {
        selectedGrid = grid
        activeMode = Modes.EDIT;
    }

    function cancel() {
        selectedGrid = null;
        activeMode = Modes.VIEW
    }

    function saveReportGrid(grid) {

        const createCmd = {
            name: grid.name,
            description: grid.description,
            externalId: toUpperSnakeCase(grid.name)
        }

        let savePromise = reportGridStore.create(createCmd);
        Promise.resolve(savePromise)
            .then(r => {
                toasts.success("Grid saved successfully")
                onSelect(r.data);
            })
            .catch(e => toasts.error("Could not save grid"));
    }

</script>

<div class="row">
    <div class="col-sm-12">
        {#if activeMode === Modes.VIEW}
            {#if _.isEmpty(grids)}
                <NoData>
                    There are no report grids, would you like to
                    <button class="btn btn-skinny"
                            on:click={() => createReportGrid()}>
                        create one?
                    </button>
                </NoData>
            {:else }
                <table class="table table-condensed table-hover small">
                    <colgroup>
                        <col width="40%">
                        <col width="40%">
                        <col width="20%">
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Grid Name</th>
                            <th>Description</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    {#each grids as grid}
                        <tr class:waltz-highlighted-row={selectedGridId === grid.id}
                            class="clickable"
                            on:click={() => onSelect(grid)}>
                            <td>
                                <button on:click={() => onSelect(grid)}
                                        class="btn btn-skinny">
                                    {grid.name}
                                </button>
                            </td>
                            <td>{grid.description}</td>
                            <td>
                                <button class="btn btn-skinny" on:click={() => editReportGrid(grid)}>
                                    <Icon name="pencil"/>Edit
                                </button>
                            </td>
                        </tr>
                    {/each}
                    <tr>
                        <td colspan="3">
                            <button class="btn btn-skinny"
                                    on:click={() => createReportGrid()}>
                                <Icon name="plus"/>Create a new report grid
                            </button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            {/if}
            <button class="btn btn-skinny"
                    on:click={onCancel}>
                Cancel
            </button>
        {:else if activeMode === Modes.EDIT}
            <h4>Creating a new grid</h4>
            <ReportGridEditor grid={selectedGrid}
                              doSave={saveReportGrid}
                              doCancel={cancel}/>
        {/if}
    </div>
</div>