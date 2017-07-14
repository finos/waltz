import * as _ from "lodash";

export function groupRelationships(ciId, rels = []) {
    return _.chain(rels)
        .flatMap(rel => ([
            {entity: rel.a, relationship: rel.relationship},
            {entity: rel.b, relationship: rel.relationship}
        ]))
        .reject(rel => rel.entity.kind === 'CHANGE_INITIATIVE' && rel.entity.id === ciId)
        .groupBy('entity.kind', 'entity.id')
        .value();
}


export function enrichRelationships(rels = [], entities = []) {
    const entitiesById = _.keyBy(entities, 'id');

    return _.chain(rels)
        .map(rel => Object.assign({}, {
            relationship: rel.relationship,
            entity: entitiesById[rel.entity.id]
        }))
        .filter(rel => rel.entity)
        .value();
}