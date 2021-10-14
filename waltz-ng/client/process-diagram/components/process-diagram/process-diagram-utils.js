import Activity from "./svg-elems/Activity.svelte";
import Event from "./svg-elems/Event.svelte";
import Decision from "./svg-elems/Decision.svelte";
import NavigationCell from "./svg-elems/NavigationCell.svelte";
import Boundary from "./svg-elems/Boundary.svelte";
import {max, min} from "d3-array";
import _ from "lodash";
import Message from "./svg-elems/sub-types/Message.svelte";
import Timer from "./svg-elems/sub-types/Timer.svelte";
import Inclusive from "./svg-elems/sub-types/Inclusive.svelte";
import Exclusive from "./svg-elems/sub-types/Exclusive.svelte";
import Parallel from "./svg-elems/sub-types/Parallel.svelte";
import {selectedApp, selectedObject} from "./diagram-store";


const padding = {
    top: 50,
    left: 100,
    right: 200
}

export function calcBounds(positions = []) {
    if (positions.length === 0) {
        return { x1: 0, x2: 100, y1: 0, y2: 100, width: 100, height: 100};
    }

    const x1 = min(positions, d => d.position.x);
    const x2 = max(positions, d => d.position.x + d.width);

    const y1 = min(positions, d => d.position.y);
    const y2 = max(positions, d => d.position.y + d.height);

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
    return `${bounds.x1 - padding.left} ${bounds.y2 - bounds.y1 - padding.top} ${bounds.width + padding.right} ${bounds.height}`;
}


const connectorLayoutAdjustments = {
    Event: {
        y: -13
    }
}

function lineToSPath(x1, y1, x2, y2, c = 0.2) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    // curve length, we don't want elongated curves - so pick the smallest
    const cl = Math.min(
        Math.abs(dy * c),
        Math.abs(dx * c));
    // middle

    const xm = dx > 172 // science
        ? x1 + dx / 16
        : x1 + dx / 2;
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

export function mkLayoutData(objects = [], positions = []) {
    const bounds = calcBounds(positions);
    const positionsByObjectId = _.keyBy(positions, d => d.objectId);

    return _
        .chain(objects)
        .map(d => {
            const basePosition = positionsByObjectId[d.objectId];
            return {
                id: d.objectId,
                x: basePosition.position.x,
                y: bounds.height - (basePosition.position.y),
                width: basePosition.width,
                height: basePosition.height,
                data: d,
                basePosition
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
        case "Text":
            return NavigationCell;
        case "Decision":
            return Decision;
        case "Boundary":
            return Boundary;
    }
}


export function findAssociatedApps(appsByDiagramMeasurableId, obj){
    const waltzId = _.get(obj, ["waltzReference", "id"]);
    return _.get(appsByDiagramMeasurableId, [waltzId], []);
}


export function toLinkExtId(obj) {
    if (_.isNull(obj)){
        return null;
    }

    const extId = obj.diagramLinkExtId;

    return _.isEmpty(extId)
        ? null
        : extId
            .replace("{", "")
            .replace("}", "");
}


export function lookupSubTypeComponent(subType) {
    if (_.isEmpty(subType)) {
        return null;
    }
    switch (subType) {
        case "Message":
            return Message;
        case "Timer":
            return Timer;
        case "Inclusive":
            return Inclusive;
        case "Exclusive":
            return Exclusive;
        case "Parallel":
            return Parallel;
        case "None":
            return null;
        default:
            console.log("Cannot find subtype for: " + subType)
            return null;
    }
}


/**
 * Moves the given element to be the last of it's siblings.
 * This causes it to be drawn last and therefore looks like
 * it has been moved to the front.
  * @param elem - the element to move
 */
export function moveToFront(elem) {
    elem.parentNode.appendChild(elem);
}

export function selectDiagramObject(obj){
    selectedObject.set(obj);
    selectedApp.set(null);
}


export function selectApplication(app){
    selectedApp.set(app);
    selectedObject.set(null);
}