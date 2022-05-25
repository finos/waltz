import {remote} from "./remote";


export function mkMeasurableRelationshipStore() {

    const findByEntityReference = (ref, force) => remote
        .fetchViewDatum(
            "GET",
            `api/measurable-relationship/${ref.kind}/${ref.id}`,
            null,
            {force});

    return {
        findByEntityReference
    };
}



export const measurableRelationshipStore = mkMeasurableRelationshipStore();