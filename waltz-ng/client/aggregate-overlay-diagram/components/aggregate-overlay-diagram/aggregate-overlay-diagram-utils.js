import _ from "lodash";
import {writable} from "svelte/store";
import {setContext} from "svelte";

export function renderOverlays(svgHolderElem, refs = [], targetSelector, setContentSize, isInstance) {

    if (!isInstance) {
        const existingContent = svgHolderElem.querySelectorAll(`${targetSelector} .content`);
        _.each(existingContent, elem => elem.parentNode.removeChild(elem));
    }

    _.each(refs, (v, k) => {
        if (!v) return;
        const cell = svgHolderElem.querySelector(`[data-cell-id='${k}']`);

        if (cell == null) {
            console.log("Cannot find cell for key:" + k);
            return;
        }

        const targetBox = cell.querySelector(targetSelector);

        if (!targetBox) {
            console.log("Cannot find target box for cell-id", k);
            return;
        }

        const contentRef = v.querySelector(".content");

        if (!contentRef) {
            console.log("Cannot find content section for copying into the target box for cell-id", k);
            return;
        }

        const boundingBox = targetBox.getBBox();

        setContentSize(boundingBox, contentRef);

        const existingContent = targetBox.querySelector(".content");
        if (existingContent) {
            targetBox.replaceChild(contentRef, existingContent);
        } else {
            targetBox.append(contentRef);
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

    setContext("hoveredCallout", hoveredCallout);
    setContext("selectedDiagram", selectedDiagram);
    setContext("selectedInstance", selectedInstance);
    setContext("callouts", callouts);
    setContext("selectedCallout", selectedCallout);
    setContext("overlayData", overlayData);
    setContext("widget", widget);

    return {
        selectedDiagram,
        selectedInstance,
        callouts,
        hoveredCallout,
        selectedCallout,
        overlayData,
        widget
    };
}