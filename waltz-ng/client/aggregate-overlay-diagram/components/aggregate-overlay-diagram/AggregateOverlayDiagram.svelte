<script>
    import {renderBulkOverlays, clearOverlayContent} from "./aggregate-overlay-diagram-utils";
    import {entity} from "../../../common/services/enums/entity";
    import {getContext} from "svelte";
    import BulkCallouts from "./callout/BulkCallouts.svelte";

    export let svg = "";
    export let primaryEntityRef;

    let svgHolderElem;

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
            $svgDetail = svgHolderElem.querySelector("svg");
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
        display: none;
    }
</style>