<script>
    import _ from "lodash";
    import Callout from "./Callout.svelte";
    import {renderOverlays} from "./aggregate-overlay-diagram-utils";
    import {entity} from "../../../common/services/enums/entity";
    import {getContext} from "svelte";

    export let svg = "";
    export let primaryEntityRef;
    export let widgetComponent;
    export let dataProvider;

    let svgHolderElem;
    let renderedWidgetRefs = {}; // this gets populated by the calls to `bind:this`
    let renderedCalloutRefs = {}; // this gets populated by the calls to `bind:this`

    $: cellDataByCellExtId = _.keyBy($dataProvider?.data, d => d.cellExternalId);

    $: {
        if (svgHolderElem && renderedWidgetRefs) {
            renderOverlays(
                svgHolderElem,
                renderedWidgetRefs,
                ".statistics-box",
                (bBox, contentRef) => {
                    contentRef.setAttribute("width", bBox.width);
                    contentRef.setAttribute("height", bBox.height);
                },
                primaryEntityRef.kind === entity.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.key);
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

{#key widgetComponent}  <!-- we want to destroy this section if the widget changes so the renderedWidgetRefs gets reset -->
    <div class="rendered-widgets">
        {#each _.keys(cellDataByCellExtId) as cellExtId, idx}
            <div bind:this={renderedWidgetRefs[cellExtId]}>
                <h4>Widget for cell: {cellExtId}</h4>
                <svelte:component this={widgetComponent}
                                  cellExtId={cellExtId}
                                  cellData={cellDataByCellExtId[cellExtId]}/>
            </div>
        {/each}
    </div>
{/key}

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

<style>
    .rendered-widgets {
        display: none;
    }

    .rendered-callouts {
        display: none;
    }
</style>