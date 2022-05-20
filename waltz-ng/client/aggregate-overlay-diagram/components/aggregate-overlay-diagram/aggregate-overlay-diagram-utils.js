import _ from "lodash";
import {writable} from "svelte/store";
import {setContext} from "svelte";
import {
    amberHex,
    blueHex,
    goldHex,
    greenHex,
    greyHex,
    lightGreyHex,
    pinkHex,
    purpleHex,
    redHex, yellowHex
} from "../../../common/colors";

export function clearContent(svgHolderElem, targetSelector) {
    console.log("clearing")
    const existingContent = svgHolderElem.querySelectorAll(`${targetSelector} .content`);
    _.each(existingContent, elem => elem.parentNode.removeChild(elem));
}


/**
 * Adds click handlers to all `.data-cell` elements.  The
 * click handler simply puts the cell id, name and any .stats svg
 * into the selectedOverlayStore.
 *
 * @param svgHolderElem
 * @param selectedOverlayStore
 */
export function addCellClickHandlers(svgHolderElem, selectedOverlayStore) {
    Array
        .from(svgHolderElem.querySelectorAll(".data-cell"))
        .forEach(sb => {
            sb.onclick = () => {
                const cellId = sb.getAttribute("data-cell-id");
                const cellName = sb.getAttribute("data-cell-name");
                const svg = sb.querySelector(".statistics-box svg");
                selectedOverlayStore.set({cellId, cellName, svg});
            };
        });
}


export function addScrollers(svgHolderElem) {
    Array
        .from(svgHolderElem.querySelectorAll(".statistics-box"))
        .forEach(sb => {
            const content = sb.querySelector(".content");
            if (content) {
                const contentHeight = content.getBoundingClientRect().height;
                const holderHeight = sb.parentElement.getBBox().height;
                if (contentHeight > holderHeight) {
                    sb.parentElement.style.overflowY = "auto";
                } else {
                    sb.parentElement.style.overflowY = "hidden";
                }
            }
        });
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

    console.log("rendering", targetSelector)
    const cells = Array.from(overlayCellsHolder.querySelectorAll(".overlay-cell"));

    cells
        .forEach(c => {
            const targetCellId = c.getAttribute("data-cell-id");
            const targetCell = svgHolderElem.querySelector(`[data-cell-id='${targetCellId}'] ${targetSelector}`);
            const contentRef = c.querySelector(".content");

            if (!targetCell) {
                console.log("Cannot find target cell for cell-id", targetCellId);
                return;
            }

            if (!contentRef) {
                console.log("Cannot find content section for copying into the target box for cell-id", targetCellId);
                return;
            }

            if (setContentSize) {
                setContentSize(
                    targetCell.getBBox(),
                    contentRef);
            }

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
    const diagramProportion = writable(9);
    const selectedCellId = writable(null);
    const selectedCellCallout = writable(null);
    const hasEditPermissions = writable(false);
    const selectedOverlay = writable(null);

    setContext("hoveredCallout", hoveredCallout);
    setContext("selectedDiagram", selectedDiagram);
    setContext("selectedInstance", selectedInstance);
    setContext("callouts", callouts);
    setContext("selectedCallout", selectedCallout);
    setContext("overlayData", overlayData);
    setContext("widget", widget);
    setContext("svgDetail", svgDetail);
    setContext("instances", instances);
    setContext("diagramProportion", diagramProportion);
    setContext("selectedCellId", selectedCellId);
    setContext("selectedCellCallout", selectedCellCallout);
    setContext("hasEditPermissions", hasEditPermissions);
    setContext("selectedOverlay", selectedOverlay);

    return {
        selectedDiagram,
        selectedInstance,
        callouts,
        hoveredCallout,
        selectedCallout,
        overlayData,
        widget,
        svgDetail,
        instances,
        diagramProportion,
        selectedCellId,
        selectedCellCallout,
        hasEditPermissions,
        selectedOverlay
    };
}


export const calloutColors = [
    greyHex,
    lightGreyHex,
    greenHex,
    blueHex,
    purpleHex,
    redHex,
    pinkHex,
    goldHex,
    amberHex,
    yellowHex
];
