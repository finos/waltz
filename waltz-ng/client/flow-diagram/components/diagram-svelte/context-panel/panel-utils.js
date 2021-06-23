import {ifPresent} from "../../../../common/function-utils";

export const Directions = {
    UPSTREAM: "UPSTREAM",
    DOWNSTREAM: "DOWNSTREAM",
};


export function toRef(d) {
    return {
        kind: d.data.kind,
        id: d.data.id
    };
}



export function prepareSaveCmd(diagram,
                               model,
                               overlay,
                               visibility,
                               positions,
                               diagramTransform,
                               overlayGroupsByGraphId) {

    const nodes = _.map(model.nodes, n => {
        return {
            entityReference: toRef(n),
            isNotable: false
        };
    });

    const flows = _.map(model.flows, f => {
        return {
            entityReference: toRef(f)
        };
    });

    const decorations = _
        .chain(model.decorations)
        .values()
        .flatten()
        .map(d => {
            return {
                entityReference: toRef(d),
                isNotable: false
            };
        })
        .value();

    const entities = _.concat(nodes, flows, decorations);

    const layoutData = {
        positions,
        diagramTransform: ifPresent(
            diagramTransform,
            x => x.toString(),
            "translate(0,0) scale(1)")
    };

    const annotations = _.map(model.annotations, a => {
        const ref = a.data.entityReference;
        return {
            entityReference: {kind: ref.kind, id: ref.id},
            note: a.data.note,
            annotationId: a.data.id
        }
    });

    const overlays = _
        .chain(overlay.groupOverlays)
        .flatMap(d => d)
        .map(d => ({
            overlayGroupId: overlayGroupsByGraphId[d.data.groupRef].data.id,
            entityReference: {id: d.data.entityReference.id, kind: d.data.entityReference.kind},
            fill: d.data.fill,
            stroke: d.data.stroke,
            symbol: d.data.symbol
        }))
        .value();

    return {
        diagramId: diagram.id,
        name: diagram.name,
        description: diagram.description,
        entities,
        annotations,
        overlays,
        layoutData: JSON.stringify(layoutData)
    };
}
