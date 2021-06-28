import {event} from "d3-selection";
import {drag} from "d3-drag";
import {positions} from "./store/layout";


export function mkDragHandler(node) {

    function dragger() {
        return (d) => {
            positions.move({id: node.id, dx: event.dx, dy: event.dy});
        };
    }

    return drag()
        .on("drag", dragger());
}


