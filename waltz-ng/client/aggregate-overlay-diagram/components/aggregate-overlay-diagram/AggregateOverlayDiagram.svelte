<script>
    import {
        renderBulkOverlays,
        addCellClickHandlers,
        clearContent
    } from "./aggregate-overlay-diagram-utils";
    import {getContext} from "svelte";
    import BulkCallouts from "./callout/BulkCallouts.svelte";
    import _ from "lodash";

    export let svg = "";

    let svgHolderElem;

    $: {
        if (svgHolderElem && $overlayData && $widget?.overlay) {
            const cellDataByCellExtId = _.keyBy(
                $overlayData,
                d => d.cellExternalId);

            clearContent(svgHolderElem, ".statistics-box");

            const globalProps = $widget.mkGlobalProps($overlayData);

            const propsByCellId = Array
                .from(svgHolderElem.querySelectorAll(".data-cell"))
                .map(cell => {
                    const sb = cell.querySelector(".statistics-box");
                    const cellId = cell.getAttribute("data-cell-id");
                    const cellProps = Object.assign(
                        {},
                        globalProps,
                        { cellData: cellDataByCellExtId[cellId]} );

                    const component = $widget.overlay;

                    new component({
                        target: sb,
                        props: cellProps
                    });

                    return {cellId, cellProps}
                })
                .reduce(
                    (acc, d) => {
                        acc[d.cellId] = d.cellProps;
                        return acc;
                    },
                    {});

            addCellClickHandlers(svgHolderElem, selectedOverlay, propsByCellId);
        }
    }

    $: {
        if (svgHolderElem && $callouts) {
            // clearContent(svgHolderElem, ".callout-box");

            setTimeout(
                () => renderBulkOverlays(
                    svgHolderElem,
                    calloutsHolder,
                    ".callout-box",
                    (bBox, contentRef) => {
                        contentRef.setAttribute("width", bBox.width);
                        contentRef.setAttribute("height", bBox.height);
                    }),
                100);
        }
    }

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let overlayData = getContext("overlayData");
    let widget = getContext("widget");
    let callouts = getContext("callouts");
    let selectedOverlay = getContext("selectedOverlay");
    let svgDetail = getContext("svgDetail");

    let calloutsHolder;


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


<style>
    .rendered-callouts {
        display: none;
    }
</style>