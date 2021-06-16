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
    import {ifPresent} from "../../../../common/function-utils";
    import model from "../store/model";
    import overlay from "../store/overlay";
    import visibility from "../store/visibility";
    import {diagramTransform, positions} from "../store/layout";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import VisibilityToggles from "./VisibilityToggles.svelte";
    import RelatedEntitiesPanel from "./RelatedEntitiesPanel.svelte";


    export let diagramId;
    let selectedApp = null;

    let savePromise;
    let selectedTab = 'context';

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }


    function toRef(d) {
        return {
            kind: d.data.kind,
            id: d.data.id
        };
    }

    $: overlayGroupsCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);
    $: overlayGroupsByGraphId = _
        .chain($overlayGroupsCall.data)
        .map(d => Object.assign({}, {id: toGraphId({kind: 'GROUP', id: d.id}), data: d}))
        .keyBy(d => d.id)
        .value();


    function prepareSaveCmd(diagram,
                            model,
                            overlay,
                            visibility,
                            positions,
                            diagramTransform,
                            overlayGroupsByGraphId) {

        const nodes = _.map(model.nodes, n => {
            return {
                entityReference: toRef(n),
                isNotable: false
            };
        });

        const flows = _.map(model.flows, f => {
            return {
                entityReference: toRef(f)
            };
        });

        const decorations = _
            .chain(model.decorations)
            .values()
            .flatten()
            .map(d => {
                return {
                    entityReference: toRef(d),
                    isNotable: false
                };
            })
            .value();

        const entities = _.concat(nodes, flows, decorations);

        const layoutData = {
            positions,
            diagramTransform: ifPresent(
                diagramTransform,
                x => x.toString(),
                "translate(0,0) scale(1)")
        };

        const annotations = _.map(model.annotations, a => {
            const ref = a.data.entityReference;
            return {
                entityReference: {kind: ref.kind, id: ref.id},
                note: a.data.note,
                annotationId: a.data.id
            }
        });

        const overlays = _
            .chain(overlay.groupOverlays)
            .flatMap(d => d)
            .map(d => ({
                overlayGroupId: overlayGroupsByGraphId[d.data.groupRef].data.id,
                entityReference: {id: d.data.entityReference.id, kind: d.data.entityReference.kind},
                fill: d.data.fill,
                stroke: d.data.stroke,
                symbol: d.data.symbol
            }))
            .value();

        return {
            diagramId: diagramId,
            name: diagram.name,
            description: diagram.description,
            entities,
            annotations,
            overlays,
            layoutData: JSON.stringify(layoutData)
        };
    }

    function save() {
        let saveCmd = prepareSaveCmd($diagram,
            $model,
            $overlay,
            $visibility,
            $positions,
            $diagramTransform,
            overlayGroupsByGraphId)

        savePromise = flowDiagramStore.save(saveCmd);
        dirty.set(false);
    }

    let activeMode = Modes.VIEW;

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
                    on:click={() => activeMode = Modes.EDIT}>
                <Icon name="pencil"/>Edit
            </button>
        </span>
        </h4>
        <p class="help-block">{$diagram.description || "No description provided"}</p>
        <div class="small text-muted">
            (<LastEdited class="small pull-right text-muted" entity={$diagram}/>)
        </div>
    {:else  if activeMode === Modes.EDIT}
        <EditFlowDiagramPanel flowDiagram={$diagram} on:cancel={() => activeMode = Modes.VIEW}/>
    {/if}
</div>

<br>
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
                <NodePanel selected={$selectedNode} on:cancel={cancel}/>
            {:else if $selectedFlow}
                <FlowPanel selected="{$selectedFlow}" on:cancel={cancel}/>
            {:else if $selectedAnnotation}
                <AnnotationPanel selected="{$selectedAnnotation}" on:cancel={cancel}/>
            {:else}
                <p class="help-block">Select a node or flow on the diagram to make changes</p>
                <DefaultPanel/>
            {/if}
        {:else if selectedTab === 'overlays'}
            <OverlayGroupsPanel {diagramId}/>
        {:else if selectedTab === 'filters'}
            <VisibilityToggles/>
        {:else if selectedTab === 'relationships'}
            <RelatedEntitiesPanel {diagramId}/>
        {/if}
    </div>
</div>

<style type="text/scss">

    @import '../../../../../style/variables';


    .save-warning{
      color: $waltz-amber;
    }
</style>