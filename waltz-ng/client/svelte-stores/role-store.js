import {remote} from "./remote";

export function mkRoleStore() {

    const findAll = (force = false) => remote
        .fetchAppList(
            "GET",
            "api/role",
            [],
            {force});

    const getViewById = (id, force = false) => remote
        .fetchViewDatum(
            "GET",
            `api/role/view/${id}`,
            [],
            {force});

    return  {
        findAll,
        getViewById
    };
}

export const roleStore = mkRoleStore();
