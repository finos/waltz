import {writable} from "svelte/store";
import {mkLayoutData} from "./process-diagram-utils";

export const connections = writable([]);
export const objects = writable([]);
export const positions = writable([])
export const layoutDataById = writable({});

export function initData(layout) {
    const positionsById = _.keyBy(
        layout.positions,
        d => d.objectId);

    const objs = _
        .chain(layout.objects)
        .filter(d => positionsById[d.objectId])
        .value();

    const layoutData = mkLayoutData(
        objs, layout.positions);

    const conns = _
        .chain(layout.connections)
        .reject(conn => conn.hidden)
        .filter(conn => layoutData[conn.startObjectId] && layoutData[conn.endObjectId])
        .value()

    positions.set(layout.positions);
    objects.set(objs);
    layoutDataById.set(layoutData);
    connections.set(conns);
}