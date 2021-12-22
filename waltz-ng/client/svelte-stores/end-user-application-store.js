
import {remote} from "./remote";

export function mkEndUserApplicationStore() {

    const findAll = (force = false) => remote
        .fetchAppList("GET", "api/end-user-application", [], {force});

    const getById = (id, force = false) => remote
        .fetchViewDatum("GET", `api/end-user-application/id/${id}`, null, {force});

    const promoteToApplication = (id, comment, force = false) => remote
        .execute("POST", `api/end-user-application/promote/${id}`, comment, {force});

    return {
        findAll,
        getById,
        promoteToApplication
    };
}

export const endUserApplicationStore = mkEndUserApplicationStore();