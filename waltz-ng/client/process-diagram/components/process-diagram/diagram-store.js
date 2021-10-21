import {derived, writable} from "svelte/store";
import {mkLayoutData} from "./process-diagram-utils";
import _ from "lodash";

export const connections = writable([]);
export const objects = writable([]);
export const positions = writable([])
export const layoutDataById = writable({});
export const appAlignments = writable([]);
export const diagramInfo = writable(null);
export const selectedObject = writable(null);
export const selectedApp = writable(null);

function resetStore() {
    selectedObject.set(null);
}

export function initData(diagram, layout, alignments) {

    resetStore();

    const positionsById = _.keyBy(
        layout.positions,
        d => d.objectId);

    const objs = _
        .chain(layout.objects)
        .filter(d => positionsById[d.objectId])
        .value();

    const layoutData = mkLayoutData(
        objs, layout.positions);

    const objectsById =  _.keyBy(objs, o =>  o.objectId);

    const conns = _
        .chain(layout.connections)
        .reject(conn => conn.hidden)
        .filter(conn => layoutData[conn.startObjectId] && layoutData[conn.endObjectId])
        .map(conn => Object.assign({}, conn, {startObject: objectsById[conn.startObjectId] , endObject: objectsById[conn.endObjectId]}))
        .value();

    positions.set(layout.positions);
    objects.set(objs);
    layoutDataById.set(layoutData);
    connections.set(conns);
    diagramInfo.set(diagram);
    appAlignments.set(alignments)
}


export const appsByDiagramMeasurableId = derived([appAlignments], ([$appAlignments]) =>  {
    return _
        .chain($appAlignments)
        .map(a => Object.assign({}, { diagramEntityId: a.diagramMeasurableRef.id, applicationRef: a.applicationRef }))
        .uniqBy(d => `${d.diagramEntityId}_${d.applicationRef.id}`)
        .orderBy(d => _.toLower(d.applicationRef.name))
        .groupBy(t => t.diagramEntityId)
        .value();
});


export const highlightedConnections = derived([connections, selectedObject], ([$connections, $selectedObject]) => {
    if (_.isNull($selectedObject)) {
        return [];
    }

    return _
        .chain($connections)
        .filter(c => c.startObjectId === $selectedObject.objectId || c.endObjectId === $selectedObject.objectId)
        .value();
});


export const highlightedActivities = derived(
    [appAlignments, selectedApp, objects], ([$appAlignments, $selectedApp, $objects]) =>
    {
        const objectsByWaltzRefId = _
            .chain($objects)
            .filter(d => d.waltzReference)
            .keyBy(d => d.waltzReference.id)
            .value();

        if (_.isNull($selectedApp)) {
            return [];
        }
        return _
            .chain($appAlignments)
            .filter(a => a.applicationRef.id === $selectedApp.id)
            .map(a => _.get(objectsByWaltzRefId, [a.diagramMeasurableRef.id], null))
            .compact()
            .uniqBy(d => d.objectId)
            .sortBy(d => _.toLower(d.name))
            .value();
    });