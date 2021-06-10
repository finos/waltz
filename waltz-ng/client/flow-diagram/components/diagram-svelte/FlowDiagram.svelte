<script>
    import NodeLayer from "./NodeLayer.svelte";
    import FlowLayer from "./FlowLayer.svelte";
    import AnnotationLayer from "./AnnotationLayer.svelte";
    import {processor, selectedAnnotation, selectedFlow, selectedNode, store} from "./diagram-model-store";
    import {event, select} from "d3-selection";
    import {zoom} from "d3-zoom";
    import ContextPanel from "./context-panel/ContextPanel.svelte";
    import model from "./store/model";
    import visibility from "./store/visibility"
    import VisibilityToggles from "./context-panel/VisibilityToggles.svelte";
    import {diagramTransform, positions} from "./store/layout";

    let elem;
    let editMode = false;
    export let doSave;

    function onSelectFlow(d) {
        $selectedNode = null;
        $selectedAnnotation = null;
        $selectedFlow = d.detail;
    }

    function onSelectNode(d) {
        $selectedFlow = null;
        $selectedAnnotation = null;
        $selectedNode = d.detail;
    }

    function onSelectAnnotation(d) {
        $selectedFlow = null;
        $selectedNode = null;
        $selectedAnnotation = d.detail;
    }

    /**
     * Pan and zoom only enabled if ctrl or meta key is held down.
     * @param commandProcessor
     */
    function setupPanAndZoom(commandProcessor) {
        function zoomed() {
            const t = event.transform;
            $diagramTransform = t;
        }

        const myZoom = zoom()
            .filter(() => event.metaKey || event.ctrlKey)
            .scaleExtent([1 / 4, 2])
            .on("zoom", zoomed);


        select("body").on("keyup.zoom", () => {
            select(elem).on(".zoom", null);
        });

        select("body").on("keydown.zoom", () => {
            const active = event.metaKey || event.ctrlKey;
            if (active) {
                select(elem)
                    .call(myZoom)
                    .on("dblclick.zoom", null);
            }
        });
    }

    $: select(elem)
        .selectAll(".wfd-flow-bucket")
        .style("display", $visibility.flowBuckets ? "" : "none");

    $: select(elem)
        .selectAll(".wfd-flow-lifecycle-REMOVED")
        .style("display", $visibility.removedFlows ? "" : "none");

    $: select(elem)
        .selectAll(".wfd-flow-lifecycle-PENDING")
        .style("display", $visibility.pendingFlows ? "" : "none");

    $: elem && setupPanAndZoom($processor);

    $: console.log("FlowDiag:", {store: $store, model: $model, positions: $positions});

</script>


{#if $store.diagramId}
<div class="col-md-8 diagram-svg">
    <svg viewBox="0 0 1100 600"
         width="100%"
         bind:this={elem}>
        <g transform={$diagramTransform}>

            {#if $visibility.annotations}
                <AnnotationLayer on:selectAnnotation={onSelectAnnotation}
                                 positions={$positions}
                                 annotations={$model.annotations}/>
            {/if}


            <FlowLayer on:selectFlow={onSelectFlow}
                       {positions}
                       {model}/>


            <NodeLayer on:selectNode={onSelectNode}
                       positions={$positions}
                       nodes={$model.nodes}
                       groups={$store.model?.groups}/>

        </g>
    </svg>
</div>
<div class="col-md-4 context-menu">
    <ContextPanel diagramId={$store.diagramId} {doSave}/>
    <VisibilityToggles/>
</div>
{/if}

<style>
    svg {
        border: 2px solid #fafafa;
    }

    .diagram-svg {
        padding: 0
    }

    .context-menu {
        padding: 10px;
    }

</style>