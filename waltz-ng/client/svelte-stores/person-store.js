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

    const findByUserId = (userId) => remote
        .fetchViewData(
            "GET",
            `api/person/user-id/${userId}`, null, null);

  const getSelf = () => remote
        .fetchViewDatum(
            "GET",
            "api/person/self", null, null);

    return {
        getByEmployeeId,
        getById,
        findByUserId,
        getSelf
    };
}

export const personStore = mkPersonStore();