<script>

    import ReportGridPicker from "./ReportGridPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {selectedGrid, ownedReportIds} from "./report-grid-store";
    import {reportGridKinds} from "./report-grid-utils";
    import ReportGridEditor from "./ReportGridEditor.svelte";
    import {toUpperSnakeCase} from "../../../common/string-utils";
    import {reportGridMemberStore} from "../../../svelte-stores/report-grid-member-store";
    import {reportGridStore} from "../../../svelte-stores/report-grid-store";
    import toasts from "../../../svelte-stores/toast-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let onGridSelect = () => console.log("selecting grid");

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let activeMode = Modes.VIEW;


    let grids = [];
    $: reportGridCall = reportGridStore.findForUser(true);
    $: grids = $reportGridCall.data;


    $: gridOwnersCall = $selectedGrid?.definition?.id && reportGridMemberStore.findByGridId($selectedGrid?.definition.id);
    $: gridOwners = $gridOwnersCall?.data || [];

    function selectGrid(grid) {
        onGridSelect(grid);
    }

    function create(grid){
        const createCmd = {
            name: grid.name,
            description: grid.description,
            externalId: toUpperSnakeCase(grid.name),
            kind: grid.kind
        }

        let savePromise = reportGridStore.create(createCmd);
        Promise.resolve(savePromise)
            .then(r => {
                toasts.success("Grid created successfully")
                selectGrid(r.data);
                activeMode = Modes.VIEW;
                reportGridCall = reportGridStore.findForUser(true);
            })
            .catch(e => toasts.error("Could not create grid"));
    }

    function update(grid){
        const updateCmd = {
            name: grid.name,
            description: grid.description,
            kind: grid.kind
        }

        let savePromise = reportGridStore.update(grid.id, updateCmd);
        Promise.resolve(savePromise)
            .then(r => {
                toasts.success("Grid updated successfully")
                selectGrid(r.data);
                activeMode = Modes.VIEW;
            })
            .catch(e => toasts.error("Could not update grid"));
    }

    function saveReportGrid(grid) {
        if(grid.id){
            update(grid);
        } else {
            create(grid);
        }
    }

    function createGrid() {

        const workingGrid = {
            name: null,
            description: null,
            externalId: null
        }

        $selectedGrid = { definition: workingGrid };
        activeMode = Modes.EDIT
    }


    function cancel(){
        activeMode = Modes.VIEW;
    }

    $: gridOwnerNames = _.map(gridOwners, d => d.userId);

</script>

<div class="row">
    <div class="col-sm-5">
        <ReportGridPicker grids={grids}
                          onCreate={createGrid}
                          onGridSelect={selectGrid}/>
    </div>
    <div class="col-sm-7">
        {#if activeMode === Modes.EDIT}
            <h4>Editing report grid:</h4>
            <ReportGridEditor grid={$selectedGrid.definition}
                              doSave={saveReportGrid}
                              doCancel={cancel}/>
        {:else if activeMode === Modes.VIEW}
            {#if $selectedGrid.definition.id}
                <h4>{$selectedGrid?.definition?.name}</h4>
                <div class:text-muted={$selectedGrid.definition?.description}>
                    {$selectedGrid.definition?.description || "No description provided"}
                </div>
                <br>
                <div>
                    Kind: {_.get(reportGridKinds[$selectedGrid?.definition?.kind], 'name', 'Unknown Kind')}
                </div>
                <div>
                    Owners:
                    {#if !_.isEmpty(gridOwners)}
                        {_.join(gridOwnerNames, '; ')}
                    {:else}
                        <span class="text-muted">None defined</span>
                    {/if}
                </div>
                <br>
                {#if _.includes($ownedReportIds, $selectedGrid.definition?.id)}
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.EDIT}>
                        <Icon name="pencil"/>Edit Grid
                    </button>
                    <div class="help-block small">
                        <Icon name="info-circle"/>To edit the columns for the grid use the 'Column Editor' tab above.
                    </div>
                {:else}
                    <div class="help-block small">
                        <Icon name="info-circle"/>You cannot edit this grid as you are not an owner.
                    </div>
                {/if}
            {:else}
                <NoData>No grid selected</NoData>
            {/if}
        {/if}
    </div>
</div>


<style>

    li {
        display: inline;
    }

</style>