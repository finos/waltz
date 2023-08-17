<script>
    import {createInitialGroup, diagramMode, DiagramModes} from "../builder/diagram-builder-store";
    import toasts from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import DiagramList from "../DiagramList.svelte";
    import {toEntityRef} from "../../../../common/entity-utils";
    import {mkGroup} from "../entity-diagram-utils";
    import {buildHierarchies} from "../../../../common/hierarchy-utils";
    import {diagramService} from "../entity-diagram-store";
    import EditDiagramDetailsPanel from "../builder/EditDiagramDetailsPanel.svelte";
    import UpdateDiagramConfirmationPanel from "../builder/UpdateDiagramConfirmationPanel.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";
    import {releaseLifecycleStatus} from "../../../../common/services/enums/release-lifecycle-status";
    import {aggregateOverlayDiagramStore} from "../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {overlayDiagramKind} from "../../../../common/services/enums/overlay-diagram-kind";

    let BuilderModes = {
        PICKER: "PICKER",
        EDIT: "EDIT",
        CREATE: "CREATE",
        UPDATE: "UPDATE",
        REMOVE: "REMOVE"
    }

    let dataInput = false;
    let workingData = "";

    let activeMode = BuilderModes.PICKER;


    const {
        selectDiagram,
        saveDiagram,
        reset,
        selectedDiagram,
        selectedGroup,
        groups,
        uploadDiagramLayout,
        updateDiagramStatus,
        populateFromExistingData
    } = diagramService;

    let diagramsCall = aggregateOverlayDiagramStore.findByKind(overlayDiagramKind.WALTZ_ENTITY_OVERLAY.key);
    $: diagrams = $diagramsCall.data || [];

    function selectOverlayDiagram(evt) {
        selectDiagram(evt.detail.id);
        activeMode = BuilderModes.EDIT;
    }

    function createNewDiagram() {
        populateFromExistingData([createInitialGroup()]);
        activeMode = BuilderModes.EDIT;
    }

    function editData() {
        workingData = JSON.stringify($groups, "", 2);
        dataInput = true;
    }

    function cancelEditLayout() {
        reset();
        activeMode = BuilderModes.PICKER;
    }

    function cancelSave() {
        activeMode = BuilderModes.EDIT;
    }

    function doSaveDiagram(diagram) {
        return saveDiagram(diagram)
            .then(() => {
                toasts.success("Successfully saved diagram");
                diagramsCall = aggregateOverlayDiagramStore.findByKind(overlayDiagramKind.WALTZ_ENTITY_OVERLAY.key, true)
                activeMode = BuilderModes.PICKER;
            })
            .catch(e => displayError("Couldn't save diagram", e))
    }

    function create(evt) {
        const diagram = evt.detail;
        doSaveDiagram(diagram);
    }

    function update() {
        doSaveDiagram($selectedDiagram);
    }


    function editDiagramDetails() {
        activeMode = BuilderModes.CREATE;
    }


    function confirmSave() {
        if ($selectedDiagram) {
            activeMode = BuilderModes.UPDATE;
        } else {
            activeMode = BuilderModes.CREATE;
        }
    }

    function populateLayoutFromData() {
        const gs = JSON.parse(workingData);

        const providedGroups = _.map(gs, d => {

            const data = d.data
                ? d.data.entityReference || toEntityRef(d.data)
                : null;

            return mkGroup(d.title, d.id, d.parentId, d.position, d.props, data)
        });

        const roots = buildHierarchies(providedGroups);

        if (_.size(roots) === 1) {
            populateFromExistingData(providedGroups);
            toasts.success("Diagram populated from data");
        } else {
            const initialGroup = createInitialGroup();
            const topLevel = _.map(roots, d => d.id);
            const childGroups = _.map(
                providedGroups,
                d => _.includes(topLevel, d.id)
                    ? Object.assign({}, d, {parentId: initialGroup.id})
                    : d);
            populateFromExistingData(_.concat(initialGroup, childGroups));
            toasts.warning("There was not a single root to this diagram so a placeholder has been added");
        }
        dataInput = false;
        workingData = "";
        activeMode = BuilderModes.EDIT;
    }

    function toggleDiagramMode() {
        if ($diagramMode === DiagramModes.EDIT) {
            $diagramMode = DiagramModes.VIEW;
        } else {
            $diagramMode = DiagramModes.EDIT;
        }
    }

    function updateStatus(newStatus) {
        updateDiagramStatus($selectedDiagram.id, newStatus.key)
            .then(() => toasts.success(`Successfully updated status to: ${newStatus.name}`))
            .catch(e => displayError(`Unable to update status to: ${newStatus.name}`, e));
    }

</script>



<div>
    {#if activeMode === BuilderModes.PICKER}
        <div class="help-block">
            <Icon name="info-circle"/> Select a diagram from the saved diagrams list below to edit the layout.
            Alternatively you can create a new diagram or import one from existing layout data.
        </div>

        {#if !dataInput}
            <DiagramList {diagrams}
                         on:select={selectOverlayDiagram}>
            </DiagramList>
            <button class="btn btn-default" on:click={createNewDiagram}>Create new diagram</button>
            <button class="btn btn-default" on:click={editData}>Populate from existing data</button>
        {:else}
            <div class="waltz-scroll-region-350">
                        <textarea class="form-control"
                                  id="data"
                                  placeholder="data"
                                  rows="10"
                                  bind:value={workingData}/>
                <div class="help-block">
                    Input data to populate diagram
                </div>
                <button class="btn btn-plain"
                        on:click={() => populateLayoutFromData()}>
                    Done
                </button>
            </div>
        {/if}
    {:else if activeMode === BuilderModes.CREATE}
        <EditDiagramDetailsPanel on:cancel={cancelSave}
                                 on:save={create}/>
    {:else if activeMode === BuilderModes.UPDATE}
        <UpdateDiagramConfirmationPanel on:cancel={cancelSave}
                                        on:save={update}/>
    {:else if activeMode === BuilderModes.EDIT}

        {#if $selectedDiagram}
            <h4>
                {$selectedDiagram.name}
            </h4>
            <table class="table table-condensed small">
                <colgroup>
                    <col width="20%"/>
                    <col width="80%"/>
                </colgroup>
                <tbody>
                    <tr>
                        <td>Description</td>
                        <td>{$selectedDiagram.description || "-"}</td>
                    </tr>
                    <tr>
                        <td>Aggregated Kind</td>
                        <td>{$selectedDiagram.aggregatedEntityKind}</td>
                    </tr>
                    <tr>
                        <td>Status</td>
                        <td>{$selectedDiagram.status}</td>
                    </tr>
                </tbody>
            </table>
        {:else}
            <h4>
                Editing Diagram Layout
            </h4>
        {/if}

        <div>
            <Toggle labelOn="Editing Diagram"
                    labelOff="Viewing Diagram"
                    state={$diagramMode === DiagramModes.EDIT}
                    onToggle={toggleDiagramMode}/>
            <div class="small help-block">
                You can toggle between view and edit modes using this control.
            </div>
        </div>

        <div class="controls">

            <button class="btn btn-default"
                    on:click={confirmSave}>
                Save Diagram
            </button>
            <button class="btn btn-default"
                    on:click={editDiagramDetails}>
                Edit Details
            </button>
            {#if $selectedDiagram}
                {#if $selectedDiagram?.status !== releaseLifecycleStatus.ACTIVE.key}
                    <button class="btn btn-default"
                            on:click={() => updateStatus(releaseLifecycleStatus.ACTIVE)}>
                        Mark Active
                    </button>
                {/if}
                {#if $selectedDiagram?.status !== releaseLifecycleStatus.DRAFT.key}
                    <button class="btn btn-default"
                            on:click={() => updateStatus(releaseLifecycleStatus.DRAFT)}>
                        Revert to Draft
                    </button>
                {/if}
                {#if $selectedDiagram?.status !== releaseLifecycleStatus.OBSOLETE.key}
                <button class="btn btn-default"
                        on:click={() => updateStatus(releaseLifecycleStatus.OBSOLETE)}>
                    Mark Obsolete
                </button>
                {/if}
            {/if}
            <button class="btn btn-default"
                    on:click={cancelEditLayout}>
                Back to Diagram Picker
            </button>
        </div>
    {/if}
</div>


<style>

    .controls {
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        align-items: flex-start;
        gap: 0.5em;
    }

</style>