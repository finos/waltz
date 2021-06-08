import {remote} from "./remote";


export function mkMeasurableCategoryStore() {
    const findAll = (ref, force) => remote
        .fetchViewList(
            "GET",
            `api/measurable-category/all`,
            null,
            {force});

    return {
        findAll,
    };
}



export const measurableCategoryStore = mkMeasurableCategoryStore();