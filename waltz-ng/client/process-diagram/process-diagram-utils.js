import Activity from "./Activity.svelte";
import IntermediateEvent from "./IntermediateEvent.svelte";
import NavigationCell from "./NavigationCell.svelte";
import {max, min} from "d3-array";


export function calcViewBox(objects = []) {
    const bounds = calcBounds(objects);
    return `${bounds.x1 - 100} ${bounds.y1 - 100} ${bounds.width + 200} ${bounds.height + 200}`;
}


export function calcBounds(objects = []) {
    const x1 = min(objects, d => d.topLeft.x);
    const y1 = min(objects, d => d.topLeft.y);
    const x2 = max(objects, d => d.bottomRight.x);
    const y2 = max(objects, d => d.bottomRight.y);
    return {
        x1,
        y1,
        x2,
        y2,
        width: x2 - x1,
        height: y2 - y1
    };

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
