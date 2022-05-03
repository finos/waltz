<script>
    import {renderBulkOverlays, clearOverlayContent} from "./aggregate-overlay-diagram-utils";
    import {entity} from "../../../common/services/enums/entity";
    import {getContext} from "svelte";
    import BulkCallouts from "./callout/BulkCallouts.svelte";
    import {event, select} from "d3-selection";
    import {zoom} from "d3-zoom";

    export let svg = "";
    export let primaryEntityRef;


    function zoomed() {
        if (! event.sourceEvent?.altKey) {
            event.sourceEvent.preventDefault();
            return;
        }
        const t = event.transform;

        $svgDetail
            .querySelector("g")
            .setAttribute("transform", t);
    }

    const myZoom = zoom()
        .on("zoom", zoomed);


    let svgHolderElem;
    let svgElem;


    $: {
        if (svgHolderElem && $overlayData) {
            if (primaryEntityRef.kind !== entity.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.key) {
                clearOverlayContent(svgHolderElem, ".statistics-box");
            }
            setTimeout(
                () => renderBulkOverlays(
                    svgHolderElem,
                    overlayCellsHolder,
                    ".statistics-box",
                    (bBox, contentRef) => {
                        contentRef.setAttribute("width", bBox.width);
                        contentRef.setAttribute("height", bBox.height);
                    }),
                100);
        }
    }

    $: {
        if (svgHolderElem && $callouts) {
            clearOverlayContent(svgHolderElem, ".outer");

            setTimeout(
                () => renderBulkOverlays(
                    svgHolderElem,
                    calloutsHolder,
                    ".outer",
                    (bBox, contentRef) => {
                        const size = bBox.height * 0.25;
                        contentRef.setAttribute("width", size);
                        contentRef.setAttribute("height", size);
                    }),
                100);
        }
    }

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let overlayData = getContext("overlayData");
    let widget = getContext("widget");
    let callouts = getContext("callouts");

    let overlayCellsHolder;
    let calloutsHolder;

    let svgDetail = getContext("svgDetail");

    $: {
        if (svgHolderElem) {
            const svgElem = svgHolderElem.querySelector("svg");
            $svgDetail = svgElem;
            select(svgElem)
                .call(myZoom)
                .on("dblclick.zoom", null);
        }
    }

</script>

<div bind:this={svgHolderElem}>
    {@html svg}
</div>


{#key $callouts}
    <div class="rendered-callouts"
         bind:this={calloutsHolder}>
        <BulkCallouts/>
    </div>
{/key}

{#key $widget}
    <div class="rendered-widgets"
         bind:this={overlayCellsHolder}>
        <svelte:component this={$widget}/>
    </div>
{/key}


<style>
    .rendered-widgets {
        display: none;
    }

    .rendered-callouts {
        xxdisplay: none;
    }
</style>