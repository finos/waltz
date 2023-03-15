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

    const findDirectsForPersonIds = (personIds = []) => remote
        .fetchViewData(
            "POST",
            `api/person/person-ids/directs`,
            personIds,
            []);

    return {
        getByEmployeeId,
        getById,
        findByUserId,
        findDirectsForPersonIds,
        getSelf
    };
}

export const personStore = mkPersonStore();