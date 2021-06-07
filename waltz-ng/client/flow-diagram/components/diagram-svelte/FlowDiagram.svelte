<script>
    import NodeLayer from "./NodeLayer.svelte";
    import FlowLayer from "./FlowLayer.svelte";
    import AnnotationLayer from "./AnnotationLayer.svelte";
    import {processor, selectedAnnotation, selectedFlow, selectedNode, store} from "./diagram-model-store";
    import {event, select} from "d3-selection";
    import {zoom} from "d3-zoom";
    import ContextPanel from "./context-panel/ContextPanel.svelte";

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
            commandProcessor([{command: "TRANSFORM_DIAGRAM", payload: t}]);
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
        .style("display", $store.visibility?.layers.flowBuckets ? "" : "none");

    $: elem && setupPanAndZoom($processor);

    $: console.log("store", $store);

</script>


{#if $store.diagramId}
<div class="col-md-8 diagram-svg">
    <svg viewBox="0 0 1100 600"
         width="100%"
         bind:this={elem}>
        <g transform={$store.layout?.diagramTransform}>

            <FlowLayer on:selectFlow={onSelectFlow}
                       positions={$store.layout?.positions}
                       decorations={$store.model?.decorations}
                       flows={$store.model?.flows}/>

            {#if $store.visibility?.layers.annotations}
                <AnnotationLayer on:selectAnnotation={onSelectAnnotation}
                                 positions={$store.layout?.positions}
                                 annotations={$store.model?.annotations}/>
            {/if}

            <NodeLayer on:selectNode={onSelectNode}
                       positions={$store.layout?.positions}
                       nodes={$store.model?.nodes}/>

        </g>
    </svg>
</div>
<div class="col-md-4 context-menu">
    <ContextPanel diagramId={$store.diagramId} {doSave}/>
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