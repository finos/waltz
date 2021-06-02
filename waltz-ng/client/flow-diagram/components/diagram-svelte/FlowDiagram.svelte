<script>
    import NodeLayer from "./NodeLayer.svelte";
    import FlowLayer from "./FlowLayer.svelte";
    import AnnotationLayer from "./AnnotationLayer.svelte";
    import {store, processor} from "./diagram-model-store";
    import {event, select} from "d3-selection";
    import {zoom} from "d3-zoom";

    let elem;

    /**
     * Pan and zoom only enabled if ctrl or meta key is held down.
     * @param commandProcessor
     */
    function setupPanAndZoom(commandProcessor) {
        function zoomed() {
            const t = event.transform;
            commandProcessor([{ command: "TRANSFORM_DIAGRAM", payload: t }]);
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

    $: elem && setupPanAndZoom($processor);

</script>

<div class="col-md-9">
    <svg viewBox="0 0 1100 600"
         width="100%"
         bind:this={elem}>
        <g transform={$store.layout?.diagramTransform}>

            {#if $store.visibility?.layers.annotations}
                <AnnotationLayer positions={$store.layout?.positions}
                                 annotations={$store.model?.annotations}/>
            {/if}

            <FlowLayer positions={$store.layout?.positions}
                       decorations={$store.model?.decorations}
                       flows={$store.model?.flows}/>

            <NodeLayer positions={$store.layout?.positions}
                       nodes={$store.model?.nodes}/>

        </g>
    </svg>
</div>
<div class="col-md-3">
    <h4>Context Menu</h4>
    <h5>Foo App</h5>
    <ul>
        <li>Add upstream</li>
        <li>Add downstream</li>
        <li>Add annotation</li>
        <li>Remove</li>
    </ul>
</div>
<hr>

<style>
    svg {
        border: 2px solid #fafafa;
    }

</style>