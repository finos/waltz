<script>
    import {createInitialGroup} from "../builder/diagram-builder-store";
    import toasts from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import DiagramList from "../DiagramList.svelte";
    import {toEntityRef} from "../../../../common/entity-utils";
    import {mkGroup} from "../entity-diagram-utils";
    import {buildHierarchies} from "../../../../common/hierarchy-utils";
    import {diagramService} from "../entity-diagram-store";
    import {diagramMode, DiagramModes} from "../builder/diagram-builder-store";
    import EditDiagramDetailsPanel from "../builder/EditDiagramDetailsPanel.svelte";
    import UpdateDiagramConfirmationPanel from "../builder/UpdateDiagramConfirmationPanel.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";

    let BuilderModes = {
        PICKER: "PICKER",
        EDIT: "EDIT",
        CREATE: "CREATE",
        UPDATE: "UPDATE"
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
    } = diagramService;


    function selectOverlayDiagram(evt) {
        selectDiagram(evt.detail.id);
        activeMode = BuilderModes.EDIT;
    }


    function createNewDiagram() {
        uploadDiagramLayout([createInitialGroup()]);
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
                activeMode = BuilderModes.PICKER;
            })
            .catch(e => displayError("Couldn't save diagram", e))
    }

    function create(evt) {
        const diagram = evt.detail;
        doSaveDiagram(diagram);
    }

    function update() {
        const diagram = _.pick($selectedDiagram, ["id", "name", "description", "diagramKind", "aggregatedEntityKind"]);
        doSaveDiagram(diagram);
    }

    function confirmSave() {
        if ($selectedDiagram) {
            activeMode = BuilderModes.UPDATE;
        } else {
            activeMode = BuilderModes.CREATE;
        }
    }

    function uploadLayout() {
        const gs = JSON.parse(workingData);

        const providedGroups = _.map(gs, d => {

            const data = d.data
                ? d.data.entityReference || toEntityRef(d.data)
                : null;

            return mkGroup(d.title, d.id, d.parentId, d.position, d.props, data)
        });

        const roots = buildHierarchies(providedGroups);

        if (_.size(roots) === 1) {
            uploadDiagramLayout(providedGroups);
            toasts.success("Diagram populated from data");
        } else {
            const initialGroup = createInitialGroup();
            const topLevel = _.map(roots, d => d.id);
            const childGroups = _.map(
                providedGroups,
                d => _.includes(topLevel, d.id)
                    ? Object.assign({}, d, {parentId: initialGroup.id})
                    : d);
            uploadDiagramLayout(_.concat(initialGroup, childGroups));
            toasts.warning("There was not a single root to this diagram so a placeholder has been added");
        }
        dataInput = false;
        workingData = "";
    }

    function toggleDiagramMode() {
        if ($diagramMode === DiagramModes.EDIT) {
            $diagramMode = DiagramModes.VIEW;
        } else {
            $diagramMode = DiagramModes.EDIT;
        }
    }

</script>



<div>
    {#if activeMode === BuilderModes.PICKER}
        <div class="help-block">
            <Icon name="info-circle"/> Select a diagram from the saved diagrams list below to edit the layout.
            Alternatively you can create a new diagram or import one from existing layout data.
        </div>

        {#if !dataInput}
            <DiagramList on:select={selectOverlayDiagram}>
            </DiagramList>
            <button class="btn btn-default" on:click={createNewDiagram}>Create new Diagram</button>
            <button class="btn btn-default" on:click={editData}>Import from Layout Data</button>
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
                        on:click={() => uploadLayout()}>
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
                Editing Layout for: {$selectedDiagram.name}
            </h4>
        {:else}
            <h4>
                Editing Layout
            </h4>
        {/if}
        <div class="help-block">
            <Icon name="info-circle"/> Use the controls below to modify the layout of this diagram.
        </div>

        <div>
            <Toggle labelOn="Editing Diagram"
                    labelOff="Viewing Diagram"
                    state={$diagramMode === DiagramModes.EDIT}
                    onToggle={toggleDiagramMode}/>
            <div class="small help-block">
                You can toggle between view and edit modes using this control.
            </div>
        </div>

        <button class="btn btn-default"
                on:click={confirmSave}>
            Save Diagram
        </button>
        <button class="btn btn-default"
                on:click={confirmSave}>
            Edit Diagram Details
        </button>
        <button class="btn btn-default"
                on:click={cancelEditLayout}>
            Back to Diagram Picker
        </button>
    {/if}
</div>
