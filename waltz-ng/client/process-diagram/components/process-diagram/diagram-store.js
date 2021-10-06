import {derived, writable} from "svelte/store";
import {mkLayoutData} from "./process-diagram-utils";
import _ from "lodash";

export const connections = writable([]);
export const objects = writable([]);
export const positions = writable([])
export const layoutDataById = writable({});
export const appAlignments = writable([]);
export const diagramInfo = writable(null);

export function initData(diagram, layout, alignments) {
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
    diagramInfo.set(diagram);
    appAlignments.set(alignments)
}

export const appCountsByDiagramMeasurableId = derived([appAlignments], ([$appAlignments]) =>  {
    return _
        .chain($appAlignments)
        .map(a => Object.assign({}, { diagramEntityId: a.diagramEntityRef.id, app: a.applicationRef }))
        .uniq()
        .countBy(t => t.diagramEntityId)
        .value()
})