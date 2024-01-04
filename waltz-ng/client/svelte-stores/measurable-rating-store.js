import {remote} from "./remote";


export function mkMeasurableRelationshipStore() {

    const getById = (id, force  = false) => remote
        .fetchViewData(
            "GET",
            `api/measurable-rating/id/${id}`,
            null,
            null,
            {force})

    const getViewById = (id, force  = false) => remote
        .fetchViewData(
            "GET",
            `api/measurable-rating/id/${id}/view`,
            null,
            null,
            {force})

    const findByApplicationSelector = (options) => remote
        .execute(
            "POST",
            "api/measurable-rating/app-selector",
            options);

    return {
        findByApplicationSelector,
        getById,
        getViewById
    };
}



export const measurableRatingStore = mkMeasurableRelationshipStore();