import {remote} from "./remote";


export function mkMeasurableStore() {
    const loadAll = (ref, force) => remote
        .fetchViewList(
            "GET",
            `api/measurable/all`,
            null,
            {force});

    return {
        loadAll
    };
}

export const measurableStore = mkMeasurableStore();