<script>

    import ReportGridPicker from "./ReportGridPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {ownedReportIds} from "./report-grid-store";
    import {reportGridKinds} from "./report-grid-utils";
    import ReportGridEditor from "./ReportGridEditor.svelte";
    import ReportGridCloneConfirmation from "./ReportGridCloneConfirmation.svelte";
    import {reportGridStore} from "../../../svelte-stores/report-grid-store";
    import {showGridSelector} from "./report-grid-ui-service";
    import toasts from "../../../svelte-stores/toast-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {entity} from "../../../common/services/enums/entity";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import {gridService} from "./report-grid-service";

    export let onGridSelect = () => console.log("selecting grid");
    export let primaryEntityRef;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        REMOVE: "REMOVE",
        CLONE: "CLONE"
    };

    let activeMode = Modes.VIEW;
    let grids = [];
    let owners = [];
    let viewers = [];
    let workingGridDef = null;

    const {gridDefinition, gridMembers, userRole} = gridService;

    $: reportGridCall = reportGridStore.findInfoForUser(true);
    $: grids = $reportGridCall.data;

    $: [owners, viewers] = _.partition($gridMembers, d => d.role === 'OWNER');

    function selectGrid(grid, isNew = false) {
        activeMode = Modes.VIEW;
        onGridSelect(grid, isNew);
    }

    function create(grid) {
        const createCmd = {
            name: grid.name,
            description: grid.description,
            kind: grid.kind,
            subjectKind: grid.subjectKind
        };

        let savePromise = reportGridStore.create(createCmd);
        Promise.resolve(savePromise)
            .then(r => {
                toasts.success("Grid created successfully");
                selectGrid(r.data, true);
            })
            .catch(e => toasts.error("Could not create report grid. " + e.error));
    }

    function update(grid) {

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
            })
            .catch(e => toasts.error("Could not update grid. " + e.error));
    }

    function clone(gridId, cloneCmd) {
        let savePromise = reportGridStore.clone(gridId, cloneCmd);
        Promise.resolve(savePromise)
            .then(r => {
                toasts.success("Grid cloned successfully")
                const grid = r.data;
                selectGrid(grid);
            })
            .catch(e => toasts.error("Could not clone grid. " + e.error));
    }

    function remove(grid) {

        let rmPromise = reportGridStore.remove(grid.id);
        Promise.resolve(rmPromise)
            .then(r => {
                toasts.success("Grid removed successfully")
                gridService.reset();
                reportGridStore.findInfoForUser(true);
            })
            .catch(e => toasts.error("Could not remove grid"));
    }

    function saveReportGrid(grid) {
        if(grid.id){
            update(grid);
        } else {
            create(grid);
        }
    }

    function createGrid() {
        workingGridDef = {
            name: null,
            description: null,
            externalId: null,
            kind: reportGridKinds.PRIVATE.key,
        }
        activeMode = Modes.EDIT;
    }

    function editGrid() {
        workingGridDef = Object.assign({}, $gridDefinition);
        activeMode = Modes.EDIT;
    }


    function cancel() {
        activeMode = Modes.VIEW;
    }

    function visitPageView() {
        $pageInfo = {
            state: "main.report-grid.view",
            params: {
                gridId: $gridDefinition.id,
                kind: primaryEntityRef.kind,
                id: primaryEntityRef.id
            }
        };
    }

</script>

<div class="row">
    {#if $showGridSelector}
        <div class="col-sm-5">
            <ReportGridPicker grids={grids}
                              onCreate={createGrid}
                              onGridSelect={selectGrid}/>
        </div>
    {/if}
    <div class:col-sm-7={$showGridSelector}
         class:col-sm-12={!$showGridSelector}>

        {#if activeMode === Modes.EDIT}
            <h4>Editing report grid:</h4>
            <ReportGridEditor grid={workingGridDef}
                              doSave={saveReportGrid}
                              doCancel={cancel}/>
        {:else if activeMode === Modes.CLONE}
            <ReportGridCloneConfirmation doClone={clone}
                                         doCancel={cancel}/>
        {:else if activeMode === Modes.VIEW || activeMode === Modes.REMOVE}
            {#if $gridDefinition?.id}
                <h4 title="Click to open in dedicated view">
                    <button on:click={() => visitPageView()}
                            class="btn btn-link">
                        {$gridDefinition?.name}
                        <Icon name="external-link"/>
                    </button>
                </h4>
                <table class="table table-condensed small">
                    <tbody>
                    <tr>
                        <td>External ID</td>
                        <td>{$gridDefinition?.externalId || "-"}</td>
                    </tr>
                    <tr>
                        <td>Subject Kind</td>
                        <td>
                            <Icon name={_.get(entity[$gridDefinition?.subjectKind], 'icon')}/>
                            {_.get(entity[$gridDefinition?.subjectKind], 'name', 'Unknown Kind')}
                        </td>
                    </tr>
                    <tr>
                        <td>Kind</td>
                        <td>
                            <Icon name={$gridDefinition?.kind === 'PUBLIC' ? "users" : "user-secret"}/>
                            {_.get(reportGridKinds[$gridDefinition?.kind], 'name', 'Unknown Kind')}</td>
                    </tr>
                    <tr id="report-grid-identifier-externalId" style="display:none;">
                        <td>externalId</td>
                        <td>{$gridDefinition?.externalId}</td>
                    </tr>
                    <tr>
                        <td>Owners</td>
                        <td>
                            {#if !_.isEmpty(owners)}
                                <div class:waltz-scroll-region-150={_.size(owners) > 10}>
                                    <ul>
                                        {#each _.orderBy(owners, d => d.user.displayName) as owner}
                                            <li>
                                                {owner.user.displayName}
                                            </li>
                                        {/each}
                                    </ul>
                                </div>
                            {:else}
                                <span class="text-muted">None defined</span>
                            {/if}
                        </td>
                    </tr>
                    <tr>
                        <td>Viewers</td>
                        <td>
                            {#if !_.isEmpty(viewers)}
                                <div class:waltz-scroll-region-150={_.size(viewers) > 10}>
                                    <ul>
                                        {#each _.orderBy(viewers, d => d.user.displayName) as viewer}
                                            <li>
                                                {viewer.user.displayName}
                                            </li>
                                        {/each}
                                    </ul>
                                </div>
                            {:else}
                                <span class="text-muted">None defined</span>
                            {/if}
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div class:text-muted={!$gridDefinition?.description}
                     style="padding-bottom: 2em;">
                    {$gridDefinition?.description || "No description provided"}
                </div>
                {#if $userRole === "OWNER"}
                    {#if activeMode === Modes.REMOVE}
                        <h4>Please confirm you would like to delete this grid?</h4>
                        <ul>
                            <li>It cannot be restored</li>
                            <li>It will be deleted for all users</li>
                            <li>It will be deleted across all views in Waltz (Org Units, App Groups, People etc.)</li>
                        </ul>
                        <button class="btn-danger btn btn-sm"
                                on:click={() => remove($gridDefinition)}>
                            Yes, delete this grid
                        </button>
                        <button class="btn-primary btn btn-sm"
                                on:click={() => activeMode = Modes.VIEW}>
                            Cancel
                        </button>
                    {/if}
                    {#if activeMode === Modes.VIEW}
                        <button class="btn btn-sm btn-primary"
                                on:click={editGrid}>
                            <Icon name="pencil"/>
                            Edit Grid Overview
                        </button>
                        <button class="btn btn-sm btn-primary"
                                on:click={() => activeMode = Modes.CLONE}>
                            <Icon name="clone"/>
                            Clone Grid
                        </button>
                        <button class="btn btn-sm btn-danger"
                                on:click={() => activeMode = Modes.REMOVE}>
                            <Icon name="trash"/>
                            Delete Grid
                        </button>
                        <div class="help-block small">
                            <Icon name="info-circle"/>
                            To edit the columns for the grid use the 'Column Editor' tab above.
                        </div>
                    {/if}
                {:else}
                    <div class="help-block small">
                        <Icon name="info-circle"/>
                        You cannot edit this grid as you are not an owner.
                    </div>
                    <button class="btn btn-sm btn-primary"
                            on:click={() => activeMode = Modes.CLONE}>
                        <Icon name="clone"/>
                        Clone Grid
                    </button>
                {/if}
            {:else}
                <NoData>Waiting for grid selection</NoData>
            {/if}
        {/if}
    </div>
</div>

<style>
     ul {
         padding-left: 1em
     }
</style>