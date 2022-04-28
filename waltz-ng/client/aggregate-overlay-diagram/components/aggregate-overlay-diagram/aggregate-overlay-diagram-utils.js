import _ from "lodash";
import {writable} from "svelte/store";
import {setContext} from "svelte";

export function clearOverlayContent(svgHolderElem, targetSelector) {
    const existingContent = svgHolderElem.querySelectorAll(`${targetSelector} .content`);
    _.each(existingContent, elem => elem.parentNode.removeChild(elem));
}


/**
 * Takes elements in the `overlayCellsHolder` marked with a class of `overlay-cell` and
 * links them to matching target cells in the `svgHolderElem`.  The matching is done via
 * an attribute, `data-cell-id`.
 *
 * For each overlay cell we search for a sub element classed as `content` and insert it
 * into the target cell using the given selector.
 *
 * @param svgHolderElem
 * @param overlayCellsHolder
 * @param targetSelector
 * @param setContentSize
 */
export function renderBulkOverlays(svgHolderElem,
                                   overlayCellsHolder = [],
                                   targetSelector,
                                   setContentSize) {


    const cells = Array.from(overlayCellsHolder.querySelectorAll(".overlay-cell"));
    cells.forEach(c => {
        const targetCellId = c.getAttribute("data-cell-id");

        const targetCell = svgHolderElem.querySelector(`[data-cell-id='${targetCellId}'] ${targetSelector}`);
        if (!targetCell) {
            console.log("Cannot find target cell for cell-id", targetCellId);
            return;
        }

        const contentRef = c.querySelector(".content");
        if (!contentRef) {
            console.log("Cannot find content section for copying into the target box for cell-id", targetCellId);
            return;
        }

        setContentSize(
            targetCell.getBBox(),
            contentRef);

        const existingContent = targetCell.querySelector(".content");
        if (existingContent) {
            targetCell.replaceChild(contentRef, existingContent);
        } else {
            targetCell.append(contentRef);
        }
    });
}


export function setupContextStores() {
    const selectedDiagram = writable(null);
    const selectedInstance = writable(null);
    const callouts = writable([]);
    const hoveredCallout = writable(null);
    const selectedCallout = writable(null);
    const overlayData = writable([]);
    const widget = writable(null);
    const svgDetail = writable(null);
    const instances = writable([]);

    setContext("hoveredCallout", hoveredCallout);
    setContext("selectedDiagram", selectedDiagram);
    setContext("selectedInstance", selectedInstance);
    setContext("callouts", callouts);
    setContext("selectedCallout", selectedCallout);
    setContext("overlayData", overlayData);
    setContext("widget", widget);
    setContext("svgDetail", svgDetail);
    setContext("instances", instances);

    return {
        selectedDiagram,
        selectedInstance,
        callouts,
        hoveredCallout,
        selectedCallout,
        overlayData,
        widget,
        svgDetail,
        instances
    };
}