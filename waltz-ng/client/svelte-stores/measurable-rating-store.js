import {remote} from "./remote";


export function mkMeasurableRelationshipStore() {

    const findByApplicationSelector = (options) => remote
        .execute(
            "POST",
            "api/measurable-rating/app-selector",
            options);

    return {
        findByApplicationSelector
    };
}



export const measurableRatingStore = mkMeasurableRelationshipStore();