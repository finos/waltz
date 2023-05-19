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

    const save = (cmd = {}) => remote
        .execute(
            "POST",
            "api/measurable-category/save-hello",
            cmd);

    return {
        findAll,
        getById,
        save
    };
}



export const measurableCategoryStore = mkMeasurableCategoryStore();