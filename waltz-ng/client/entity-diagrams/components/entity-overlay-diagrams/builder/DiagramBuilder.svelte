<script>

    import DiagramBuildView from "./DiagramBuildView.svelte";
    import {diagramService,} from "../entity-diagram-store";
    import {createInitialGroup} from "./diagram-builder-store"
    import DiagramControls from "../builder-controls/DiagramBuilderControls.svelte";
    import {buildHierarchies} from "../../../../common/hierarchy-utils";
    import DiagramTreeSelector from "./DiagramTreeSelector.svelte";
    import _ from "lodash";
    import DiagramView from "../DiagramView.svelte";
    import GroupDetailsPanel from "../builder-controls/GroupDetailsPanel.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";
    import {prettyHTML} from "../../../../system/svelte/nav-aid-builder/nav-aid-utils";
    import {mkGroup} from "../entity-diagram-utils";
    import toasts from "../../../../svelte-stores/toast-store";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import DiagramList from "../DiagramList.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EditDiagramDetailsPanel from "./EditDiagramDetailsPanel.svelte";
    import {displayError} from "../../../../common/error-utils";
    import UpdateDiagramConfirmationPanel from "./UpdateDiagramConfirmationPanel.svelte";
    import {toEntityRef} from "../../../../common/entity-utils";

    let items = [];

    let html = "";
    let data = "";
    let vizElem;

    let editing = true;
    let dataInput = false;
    let workingData = "";

    const {selectDiagram, saveDiagram, reset, selectedDiagram, selectedGroup, groupData, groups, groupsWithData, diagramLayout, uploadDiagramLayout, selectGroup} = diagramService;

    let BuilderModes = {
        PICKER: "PICKER",
        EDIT: "EDIT",
        CREATE: "CREATE",
        UPDATE: "UPDATE"
    }

    let activeMode = BuilderModes.PICKER;

    function selectOverlayDiagram(evt) {
        selectDiagram(evt.detail.id);
        activeMode = BuilderModes.EDIT;
    }

    function selectOverlayGroup(group) {
        selectGroup(group)
    }

    function deselectGroup() {
        selectOverlayGroup(null);
    }

    function uploadLayout() {
        const gs = JSON.parse(workingData);

        const providedGroups = _.map(gs, d => mkGroup(d.title, d.id, d.parentId, d.position, d.props, d.data ? toEntityRef(d.data) : null));

        const roots = buildHierarchies(providedGroups);

        console.log({roots})
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
        activeMode = BuilderModes.EDIT;
        workingData = "";
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

    $: {
        if (vizElem && $groups && !editing) {
            // using a timeout to give innerHTML chance to be updated
            // before we copy it
            setTimeout(() => {
                html = prettyHTML(vizElem.innerHTML);
                data = JSON.stringify($groups, "", 2);
            });
        }
    }

    // $: dataCall = $overlayDataCall;
    // $: overlayData = $dataCall?.data;
    // $: cellDataByCellExtId = _.keyBy(
    //     overlayData?.cellData,
    //     d => d.cellExternalId);
    //
    // $: console.log({overlayData, cellDataByCellExtId});

    $: console.log({groupData: $selectedDiagram, data: $groupData});

</script>



<PageHeader icon="picture-o"
            name="Diagram Builder">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Diagram Builder</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">

        {#if activeMode === BuilderModes.PICKER}
            <div class="col-md-12">

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
            </div>

        {:else if activeMode === BuilderModes.CREATE}
            <div class="col-sm-12">
                <EditDiagramDetailsPanel on:cancel={cancelSave}
                                         on:save={create}/>
            </div>
        {:else if activeMode === BuilderModes.UPDATE}
            <div class="col-sm-12">
                <UpdateDiagramConfirmationPanel on:cancel={cancelSave}
                                                on:save={update}/>
            </div>
        {:else if activeMode === BuilderModes.EDIT}

            <div class="col-sm-12">
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
                            state={editing}
                            onToggle={() => editing = !editing}/>
                    <div class="small help-block">
                        You can toggle between view and edit modes using this control.
                    </div>
                </div>

                <button class="btn btn-default"
                        on:click={confirmSave}>
                    Save Diagram
                </button>
                <button class="btn btn-default"
                        on:click={cancelEditLayout}>
                    Back to Diagram Picker
                </button>

                <hr>

            </div>


            <div style="padding-top: 1em">
                <div class="col-sm-8"
                     bind:this={vizElem}>
                    {#if editing}
                        <DiagramBuildView group={$diagramLayout}>
                        </DiagramBuildView>
                    {:else}
                        <DiagramView group={$diagramLayout}>
                        </DiagramView>
                    {/if}
                </div>

                <div class="col-sm-4">
                    <h4>Structure</h4>
                    <DiagramTreeSelector groups={$groupsWithData}
                                         onSelect={selectOverlayGroup}
                                         onDeselect={deselectGroup}/>
                    <hr>
                    {#if editing}
                        <DiagramControls/>
                    {:else}
                        <GroupDetailsPanel/>
                    {/if}
                </div>
            </div>

            {#if !editing}
                <div class="col-sm-6"
                     style="margin-top: 2em">
                    <div class="waltz-scroll-region-350">
                        <pre>{html}</pre>
                    </div>
                </div>
                <div class="col-sm-6"
                     style="margin-top: 2em">
                    <div class="waltz-scroll-region-350">
                        <pre>{data}</pre>
                    </div>
                </div>
            {/if}
        {/if}
    </div>
</div>