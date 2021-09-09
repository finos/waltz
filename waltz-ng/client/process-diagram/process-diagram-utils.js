import Activity from "./Activity.svelte";
import IntermediateEvent from "./IntermediateEvent.svelte";
import NavigationCell from "./NavigationCell.svelte";
import {max, min} from "d3-array";


export function calcViewBox(objects = []) {
    const minX = min(objects, d => d.topLeft.x);
    const maxX = max(objects, d => d.bottomRight.x);
    const minY = min(objects, d => d.topLeft.y);
    const maxY = max(objects, d => d.bottomRight.y);

    return `${minX - 100} ${minY - 100} ${(maxX - minX) + 100} ${(maxY - minY) + 100}`;
}


export function toComp(obj) {
    switch (obj.stereotype) {
        case "Activity":
            return Activity;
        case "IntermediateEvent":
            return IntermediateEvent;
        case "NavigationCell":
            return NavigationCell;
    }
}


export function calcRectAttrs(obj) {
    return {
        height: Math.abs(obj.bottomRight.y - obj.topLeft.y),
        width: Math.abs(obj.bottomRight.x - obj.topLeft.x)
    };
}
