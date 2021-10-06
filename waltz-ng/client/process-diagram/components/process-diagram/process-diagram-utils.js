import Activity from "./Activity.svelte";
import Event from "./Event.svelte";
import Decision from "./Decision.svelte";
import TextCell from "./TextCell.svelte";
import Boundary from "./Boundary.svelte";
import {max, min} from "d3-array";


export function calcBounds(positions = []) {
    if (positions.length === 0) {
        return { x1: 0, x2: 100, y1: 0, y2: 100, width: 100, height: 100};
    }
    const x1 = min(positions, d => d.topLeft.x);
    const y1 = min(positions, d => d.topLeft.y);
    const x2 = max(positions, d => d.bottomRight.x);
    const y2 = max(positions, d => d.bottomRight.y);
    return {
        x1,
        y1,
        x2,
        y2,
        width: x2 - x1,
        height: y2 - y1
    };
}


export function calcViewBox(positions = []) {
    const bounds = calcBounds(positions);
    return `${bounds.x1 - 100} ${bounds.y2 - bounds.y1 - 50} ${bounds.width + 200} ${bounds.height}`;
}


const connectorLayoutAdjustments = {
    Event: {
        y: -13
    }
}


export function mkConnectorPoints(layoutById, conn) {
    const start = layoutById[conn.startObjectId];
    const end = layoutById[conn.endObjectId];

    const startAdjustment = _.get(connectorLayoutAdjustments, [start.data.objectType]);
    const endAdjustment = _.get(connectorLayoutAdjustments, [end.data.objectType]);

    const x1 = start.x + start.width + _.get(startAdjustment, ["x"] , 0);
    const y1 = start.y + start.height / 2 + _.get(startAdjustment, ["y"] , 0);

    const x2 = end.x + _.get(endAdjustment, ["x"] , 0);
    const y2 = end.y + end.height / 2 + _.get(endAdjustment, ["y"] , 0);

    console.log({start, end, startAdjustment, endAdjustment})

    return `${x1},${y1} ${x2},${y2}`;
}

function lineToSPath(x1, y1, x2, y2, c = 0.2) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    // curve length, we don't want elongated curves - so pick the smallest
    const cl = Math.min(
        Math.abs(dy * c),
        Math.abs(dx * c));
    // middle
    const xm = x1 + dx / 2;
    // start and ending points of the curves,
    // ..the ternary expr (?:) on the end ensures we are adding/removing as appropriate
    const x1a = xm + cl * (x1 > x2 ? 1 : -1);
    const x2a = xm - cl * (x1 > x2 ? 1 : -1);
    const y1a = y1 + cl * (y1 > y2 ? -1 : 1);
    const y2a = y2 - cl * (y1 > y2 ? -1 : 1);
    // list of svg path commands
    const cmds = [
        `M${x1} ${y1}`,  // start pos
        `L${x1a} ${y1}`, // start horiz
        `Q${xm} ${y1}, ${xm} ${y1a}`, // curve to vert
        `L${xm} ${y2a}`, // vert
        `Q${xm} ${y2}, ${x2a} ${y2}`, // curve to horiz
        `L${x2} ${y2}`, // end horiz
    ];
    // concat to make final command str
    return cmds.join(" ");
}

export function mkConnectorPath(layoutById, conn) {
    const start = layoutById[conn.startObjectId];
    const end = layoutById[conn.endObjectId];

    const startAdjustment = _.get(connectorLayoutAdjustments, [start.data.objectType]);
    const endAdjustment = _.get(connectorLayoutAdjustments, [end.data.objectType]);

    const x1 = start.x + start.width + _.get(startAdjustment, ["x"] , 0);
    const y1 = start.y + start.height / 2 + _.get(startAdjustment, ["y"] , 0);

    const x2 = end.x + _.get(endAdjustment, ["x"] , 0);
    const y2 = end.y + end.height / 2 + _.get(endAdjustment, ["y"] , 0);

    return lineToSPath(x1, y1, x2, y2);
}


const objectLayoutAdjustments = {
    Event: {
        y: -10
    },
    Decision: {
        y: 0 //-18
    }
}



export function mkLayoutData(objects = [], positions = []) {
    const bounds = calcBounds(positions);
    const positionsByObjectId = _.keyBy(positions, d => d.objectId);

    return _
        .chain(objects)
        .map(d => {
            const adjustment =  objectLayoutAdjustments[d.objectType];
            const basePosition = positionsByObjectId[d.objectId];
            return {
                id: d.objectId,
                x: basePosition.topLeft.x + _.get(adjustment, ["x"], 0),
                y: bounds.height - (basePosition.topLeft.y + _.get(adjustment, ["y"], 0)),
                width: Math.abs(basePosition.bottomRight.x - basePosition.topLeft.x) + _.get(adjustment, ["width"], 0),
                height: Math.abs(basePosition.bottomRight.y - basePosition.topLeft.y) + _.get(adjustment, ["height"], 0),
                data: d
            };
        })
        .keyBy(d => d.id)
        .value();
}


export function toComp(obj) {
    switch (obj.objectType) {
        case "Activity":
            return Activity;
        case "Event":
            return Event;
        case "NavigationCell":
        case "Text":
            return TextCell;
        case "Decision":
            return Decision;
        case "Boundary":
            return Boundary;
    }
}


export function calcRectAttrs(obj) {
    return {
        height: Math.abs(obj.bottomRight.y - obj.topLeft.y),
        width: Math.abs(obj.bottomRight.x - obj.topLeft.x)
    };
}
