import {remote} from "./remote";


export function mkStore() {
    const getById = (id, force) => remote
        .fetchViewList(
            "GET",
            `api/rating-scheme/id/${id}`,
            null,
            {force});

    const loadAll = (force) => remote
        .fetchViewList(
            "GET",
            `api/rating-scheme`,
            null,
            {force});

    return {
        getById,
        loadAll
    };
}

export const ratingSchemeStore = mkStore();