import {remote} from "./remote";


export function mkStore() {
    const getById = (id, force) => remote
        .fetchViewList(
            "GET",
            `api/rating-scheme/id/${id}`,
            null,
            {force});

    return {
        getById
    };
}

export const ratingSchemeStore = mkStore();