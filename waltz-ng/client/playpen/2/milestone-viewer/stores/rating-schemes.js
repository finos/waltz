import {remote} from "../../../../svelte-stores/remote";


export function mkRatingSchemeStore() {
    const findItemsForScheme = (id, force) => remote
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