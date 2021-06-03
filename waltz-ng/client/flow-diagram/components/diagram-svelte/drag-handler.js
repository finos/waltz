import {event, select} from "d3-selection";
import {drag} from "d3-drag";


export function mkDragHandler(node, processor) {
    let dragStartPos = null;

    function dragStarted() {
        dragStartPos = { x: event.x, y: event.y };
        return select(this)
            .raise()
            .classed("wfd-active", true);
    }


    function dragger() {
        return (d) => {
            const cmd = {
                command: "MOVE",
                payload: {id: node.id, dx: event.dx, dy: event.dy}
            };
            processor([cmd]);
        };
    }

    function dragEnded(d) {
        const noMove = dragStartPos.x === event.x && dragStartPos.y === event.y;
        if (noMove) {
            console.log("No move")
        }

        return select(this)
            .classed("wfd-active", false);
    }

    return drag()
        .on("start.foo", dragStarted)
        .on("drag.foo", dragger()) //commandProcessor))
        .on("end.foo", dragEnded);
}


