import {remote} from "./remote";


export function mkMeasurableCategoryStore() {
    const findAll = (force) => remote
        .fetchAppList(
            "GET",
            "api/measurable-category/all",
            null,
            {force});

    return {
        findAll,
    };
}



export const measurableCategoryStore = mkMeasurableCategoryStore();