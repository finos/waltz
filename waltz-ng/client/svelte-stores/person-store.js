import {remote} from "./remote";

export function mkPersonStore() {

    const getById = (id, force) => {
        return remote
            .fetchViewData(
                "GET",
                `api/person/id/${id}`,
                null,
                force);
    };

    const getByEmployeeId = (empId) => remote
        .fetchViewData(
            "GET",
            `api/person/employee-id/${empId}`, null, null);

    const getSelf = () => remote
        .fetchViewDatum(
            "GET",
            "api/person/self", null, null);

    return {
        getByEmployeeId,
        getById,
        getSelf
    };
}

export const personStore = mkPersonStore();