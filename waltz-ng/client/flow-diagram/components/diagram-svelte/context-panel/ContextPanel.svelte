<script>
    import {selectedAnnotation, selectedFlow, selectedNode} from "../diagram-model-store";
    import NodePanel from "./NodePanel.svelte";
    import DefaultPanel from "./DefaultPanel.svelte";
    import FlowPanel from "./FlowPanel.svelte";
    import LastEdited from "../../../../common/svelte/LastEdited.svelte";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";
    import AnnotationPanel from "./AnnotationPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import OverlayGroupsPanel from "./OverlayGroupsPanel.svelte";
    import dirty from "../store/dirty";
    import EditFlowDiagramPanel from "./EditFlowDiagramOverviewPanel.svelte";
    import {diagram} from "../store/diagram";
    import _ from "lodash";
    import model from "../store/model";
    import overlay from "../store/overlay";
    import visibility from "../store/visibility";
    import {diagramTransform, positions} from "../store/layout";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import VisibilityToggles from "./VisibilityToggles.svelte";
    import RelatedEntitiesPanel from "./RelatedEntitiesPanel.svelte";
    import {userStore} from "../../../../svelte-stores/user-store";
    import CloneDiagramSubPanel from "./CloneDiagramSubPanel.svelte";
    import {prepareSaveCmd} from "./panel-utils";
    import RemoveDiagramSubPanel from "./RemoveDiagramSubPanel.svelte";


    export let diagramId;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        CLONE: "CLONE"
    }

    let userCall = userStore.load();
    $: roles = $userCall.data?.roles;
    $: canEdit = _.isNil($diagram.editorRole) || _.includes(roles, $diagram.editorRole);

    let savePromise;
    let selectedApp = null;
    let selectedTab = 'context';
    let activeMode = Modes.VIEW;

    $: overlayGroupsCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);
    $: overlayGroupsByGraphId = _
        .chain($overlayGroupsCall.data)
        .map(d => Object.assign({}, {id: toGraphId({kind: 'GROUP', id: d.id}), data: d}))
        .keyBy(d => d.id)
        .value();

    function save() {
        let saveCmd = prepareSaveCmd(
            $diagram,
            $model,
            $overlay,
            $visibility,
            $positions,
            $diagramTransform,
            overlayGroupsByGraphId)

        savePromise = flowDiagramStore.save(saveCmd);
        dirty.set(false);
    }

    function cancel() {
        $selectedNode = null;
        $selectedFlow = null;
        $selectedAnnotation = null;
    }

</script>

<!-- Diagram title -->
<div style="padding-bottom: 1px">
    {#if activeMode === Modes.VIEW}
        <h4>
            {$diagram.name}
            <span class="small">
                <button class="tn btn-skinny"
                        on:click={() => activeMode = Modes.CLONE}>
                <Icon name="clone"/>Clone
            </button>
            </span>
            {#if canEdit}
            <span class="small">
                |
                <button class="tn btn-skinny"
                    on:click={() => activeMode = Modes.EDIT}>
                    <Icon name="pencil"/>Edit
                </button>
                |
                <button class="tn btn-skinny"
                        on:click={() => activeMode = Modes.REMOVE}>
                    <Icon name="trash"/>Remove
                </button>
            </span>
            {/if}
        </h4>
        <p class="help-block">{$diagram.description || "No description provided"}</p>
        <div class="small text-muted">
            (<LastEdited class="small pull-right text-muted" entity={$diagram}/>)
        </div>
        <br>
        {#if canEdit}
            {#if $dirty}
                <span class="help-block">
                    <span class="save-warning">
                        <Icon name="exclamation-circle"/>
                    </span>
                    Changes have been made to this diagram, if you do not
                        <button class="btn btn-skinny"
                                on:click={() => save()}>
                            <strong>
                                save
                            </strong>
                        </button>
                    them they will be lost.
                </span>
                    {/if}
                {:else}
                <span class="help-block">
                    <span class="save-warning">
                        <Icon name="exclamation-circle"/>
                    </span>
                    You do not have permission to edit this diagram, any changes made will be lost.
                    You may wish to
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.CLONE}>
                        <strong>
                            clone this diagram
                        </strong>
                    </button>
                    for an editable version.
                </span>
        {/if}
    {:else  if activeMode === Modes.EDIT}
        <EditFlowDiagramPanel flowDiagram={$diagram} on:cancel={() => activeMode = Modes.VIEW}/>
        <br>
    {:else if activeMode === Modes.CLONE}
        <CloneDiagramSubPanel {diagramId} on:cancel={() => activeMode = Modes.VIEW}/>
        <br>
    {:else if activeMode === Modes.REMOVE}
        <RemoveDiagramSubPanel {diagramId} on:cancel={() => activeMode = Modes.VIEW}/>
        <br>
    {/if}
</div>

<!--Tabs for context, overlays and filters-->
<div class="waltz-tabs">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="context"
           id="context">
    <label class="wt-label"
           for="context">
        <span>Context</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="overlays"
           id="overlays">
    <label class="wt-label"
           for="overlays">
        <span>Overlays</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="filters"
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>Filters</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="relationships"
           id="relationships">
    <label class="wt-label"
           for="relationships">
        <span>Relationships</span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'context'}
            {#if $selectedNode}
                <NodePanel selected={$selectedNode}
                           on:cancel={cancel}
                           {canEdit}/>
            {:else if $selectedFlow}
                <FlowPanel selected="{$selectedFlow}"
                           on:cancel={cancel}
                           {canEdit}/>
            {:else if $selectedAnnotation}
                <AnnotationPanel selected="{$selectedAnnotation}"
                                 on:cancel={cancel}
                                 {canEdit}/>
            {:else}
                <p class="help-block">Select a node or flow on the diagram to make changes</p>
                <DefaultPanel {canEdit}/>
            {/if}
        {:else if selectedTab === 'overlays'}
            <OverlayGroupsPanel {diagramId} {canEdit}/>
        {:else if selectedTab === 'filters'}
            <VisibilityToggles/>
        {:else if selectedTab === 'relationships'}
            <RelatedEntitiesPanel {diagramId} {canEdit}/>
        {/if}
    </div>
</div>

<style type="text/scss">

    @import '../../../../../style/variables';


    .save-warning{
      color: $waltz-amber;
    }
</style>