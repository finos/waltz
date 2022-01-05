import {remote} from "./remote";

export function mkRoleStore() {

    const findAll = (force = false) => remote
        .fetchAppList(
            "GET",
            "api/role",
            [],
            {force});

    return  {
        findAll
    }
}

export const roleStore = mkRoleStore();
