<script>
    import {
        addCellClickHandlers,
        addSectionHeaderClickHandlers,
        clearContent,
        RenderModes
    } from "./aggregate-overlay-diagram-utils";
    import {getContext} from "svelte";
    import _ from "lodash";
    import {select, selectAll} from "d3-selection";
    import Callout from "./callout/Callout.svelte";
    import {hoveredCallout} from "../../aggregate-overlay-diagram-store";
    import Popover from "../../../svelte-stores/popover-store";

    const selectedOverlayCell = getContext("selectedOverlay");


    export let svg = "";

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let overlayData = getContext("overlayData");
    let widget = getContext("focusWidget");
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
                $overlayData.cellData,
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

                    let bBox = sb.getBoundingClientRect();

                    const {width, height} = bBox;

                    const customProps = {
                        cellData: cellDataByCellExtId[cellId],
                        renderMode: RenderModes.OVERLAY,
                        height,
                        width
                    };

                    const cellProps = Object.assign(
                        {},
                        globalProps,
                        customProps);

                    const component = $widget.overlay;

                    new component({
                        target: sb,
                        props: cellProps
                    });

                    return {
                        cellId,
                        cellProps
                    };
                })
                .reduce(
                    (acc, d) => {
                        acc[d.cellId] = d.cellProps;
                        return acc;
                    },
                    {});

            addCellClickHandlers(
                svgHolderElem,
                selectedOverlay,
                propsByCellId);
        }
    }


    $: {
        if (svgHolderElem && $callouts) {
            clearContent(svgHolderElem, ".callout-box");

            const calloutsByCellId = _.keyBy($callouts, c => c.cellExternalId)

            let dataCells = svgHolderElem.querySelectorAll(".data-cell");
            let headerCells = svgHolderElem.querySelectorAll(".entity-group-box");

            const propsByCellId = Array
                .from(_.union(headerCells, dataCells))
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

            addCellClickHandlers(svgHolderElem, selectedOverlay, propsByCellId);
            addSectionHeaderClickHandlers(svgHolderElem, selectedOverlay, propsByCellId);
        }
    }


    $: {
        if (svgHolderElem) {
            // $svgDetail = svgHolderElem.querySelector("svg");
            $svgDetail = svgHolderElem;
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
        selectAll('.data-cell').classed("inset", false);
        selectAll('.entity-group-box').classed("inset", false);

        if ($selectedOverlay) {
            select(`[data-cell-id=${$selectedOverlay.cellId}]`).classed("inset", true);
        }
    }

    // toggle popover
    $: {
        if ($selectedOverlay && $widget) {

            const component = $widget.overlay;

            const cell = svgHolderElem.querySelector(`[data-cell-id=${$selectedOverlay.cellId}]`);
            const cellName = cell.getAttribute("data-cell-name");
            const elem = cell.querySelector(".content");

            if (elem) {
                const props = Object.assign(
                    {},
                    $selectedOverlayCell?.props,
                    { renderMode: RenderModes.FOCUSED });

                const popover = {
                    title: cellName,
                    props,
                    component
                };

                Popover.add(popover);
            }
        }
    }

    // no data indication
    $: {
        if (svgHolderElem && $overlayData && $selectedInstance == null) {

            const cellsWithData = _
                .chain($overlayData.cellData)
                .map(d => d.cellExternalId)
                .value();

            selectAll(".data-cell")
                .classed("no-data", function () {
                    const dataCellId = select(this).attr("data-cell-id");
                    return !_.includes(cellsWithData, dataCellId);
                });
        }
    }

</script>

<div id="diagram-capture"
     bind:this={svgHolderElem}>
    {@html svg}
</div>
