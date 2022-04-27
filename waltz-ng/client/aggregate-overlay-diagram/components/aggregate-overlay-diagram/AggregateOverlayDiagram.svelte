<script>
    import _ from "lodash";
    import Callout from "./Callout.svelte";
    import {renderOverlays, renderOverlaysNew} from "./aggregate-overlay-diagram-utils";
    import {entity} from "../../../common/services/enums/entity";
    import {getContext} from "svelte";

    export let svg = "";
    export let primaryEntityRef;

    let svgHolderElem;
    let renderedWidgetRefs = {}; // this gets populated by the calls to `bind:this`
    let renderedCalloutRefs = {}; // this gets populated by the calls to `bind:this`

    $: cellDataByCellExtId = _.keyBy($overlayData, d => d.cellExternalId);

    $: {
        if (svgHolderElem && $overlayData) {
            setTimeout(
                () => renderOverlaysNew(
                    svgHolderElem,
                    overlayCellsHolder,
                    ".statistics-box",
                    (bBox, contentRef) => {
                        contentRef.setAttribute("width", bBox.width);
                        contentRef.setAttribute("height", bBox.height);
                    },
                    primaryEntityRef.kind === entity.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.key),
                100);
        }
    }


    $: {
        if (svgHolderElem && renderedCalloutRefs && $callouts) {
            renderOverlays(
                svgHolderElem,
                renderedCalloutRefs,
                ".outer",
                (bBox, contentRef) => {
                    const size = bBox.height * 0.25;
                    contentRef.setAttribute("width", size);
                    contentRef.setAttribute("height", size);
                });
        }
    }

    let callouts = getContext("callouts");
    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let hoveredCallout = getContext("hoveredCallout");
    let overlayData = getContext("overlayData");
    let widget = getContext("widget");

    let overlayCellsHolder;

    function hoverCallout(evt) {
        $hoveredCallout = evt.detail;
    }

    function leaveCallout(evt) {
        $hoveredCallout = null;
    }

</script>

<div bind:this={svgHolderElem}>
    {@html svg}
</div>


{#key $selectedInstance}
    {#if !_.isEmpty($callouts)}
        <div class="rendered-callouts">
            {#each $callouts as callout, idx}
                <div bind:this={renderedCalloutRefs[callout.cellExternalId]}>
                    <h4>Callout for cell: {callout.cellExternalId}</h4>
                    <Callout {callout}
                             label={idx + 1}
                             on:hover={hoverCallout}
                             on:leave={leaveCallout}/>
                </div>
            {/each}
        </div>
    {/if}
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