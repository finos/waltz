import {remote} from "./remote";


export function mkMeasurableCategoryStore() {
    const findAll = (force) => remote
        .fetchAppList(
            "GET",
            "api/measurable-category/all",
            null,
            {force});

    const getById = (id, force) => remote
        .fetchViewDatum(
            "GET",
            `api/measurable-category/id/${id}`,
            null,
            {force});

    return {
        findAll,
        getById
    };
}



export const measurableCategoryStore = mkMeasurableCategoryStore();