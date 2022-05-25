<script>
    import {
        renderBulkOverlays,
        addCellClickHandlers,
        clearContent
    } from "./aggregate-overlay-diagram-utils";
    import {getContext} from "svelte";
    import _ from "lodash";
    import {select, selectAll} from "d3-selection";
    import Callout from "./callout/Callout.svelte";
    import {hoveredCallout} from "../../aggregate-overlay-diagram-store";

    export let svg = "";

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let overlayData = getContext("overlayData");
    let widget = getContext("widget");
    let callouts = getContext("callouts");
    let selectedOverlay = getContext("selectedOverlay");
    let svgDetail = getContext("svgDetail");
    let relatedBackingEntities = getContext("relatedBackingEntities");
    let cellIdsExplicitlyRelatedToParent = getContext("cellIdsExplicitlyRelatedToParent");

    let svgHolderElem;
    let calloutsHolder;

    $: {
        if (svgHolderElem && $overlayData && $widget?.overlay) {
            const cellDataByCellExtId = _.keyBy(
                $overlayData,
                d => d.cellExternalId);

            clearContent(svgHolderElem, ".statistics-box");

            const globalProps = $widget.mkGlobalProps
                ? $widget.mkGlobalProps($overlayData)
                : {};

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
            clearContent(svgHolderElem, ".callout-box");

            const calloutsByCellId = _.keyBy($callouts, c => c.cellExternalId)

            Array
                .from(svgHolderElem.querySelectorAll(".data-cell"))
                .map(cell => {
                    const sb = cell.querySelector(".callout-box");
                    const cellId = cell.getAttribute("data-cell-id");

                    const component = Callout

                    let callout = calloutsByCellId[cellId];

                    let cellProps = {
                        callout: callout,
                        hoveredCallout: $hoveredCallout,
                        label: _.indexOf($callouts, callout) + 1
                    };
                    new component({
                        target: sb,
                        props: cellProps
                    })

                    return {cellId, cellProps}
                })
                .reduce((acc, d) => {
                        acc[d.cellId] = d.cellProps;
                        return acc;
                    },
                    {}
                );
        }
    }


    $: {
        if (svgHolderElem) {
            $svgDetail = svgHolderElem.querySelector("svg");
        }
    }

    // highlight explicitly related cells
    $: {
        if (svgHolderElem && $cellIdsExplicitlyRelatedToParent) {
            $cellIdsExplicitlyRelatedToParent
                .forEach(cellId => select(`[data-cell-id=${cellId}]`)
                    .classed("show-related-entity-indicator", true));
        }
    }

    // toggle inset indication
    $: {
        if ($selectedOverlay) {
            selectAll('.data-cell').classed("inset", false);
            select(`[data-cell-id=${$selectedOverlay.cellId}]`).classed("inset", true);
        }
    }

</script>

<div bind:this={svgHolderElem}>
    {@html svg}
</div>
