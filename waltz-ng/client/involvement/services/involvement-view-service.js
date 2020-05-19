
export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/involvement-view`;

    const findAllInvolvementsByEmployeeId = (employeeId) => $http
        .get(`${BASE}/employee/${employeeId}`)
        .then(result => result.data);

    return {
        findAllInvolvementsByEmployeeId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "InvolvementViewService";

export const InvolvementViewService_API = {
    findAllInvolvementsByEmployeeId: {
        serviceName,
        serviceFnName: "findAllInvolvementsByEmployeeId",
        description: "find all direct and inherited involvements with person for employeeId"
    }
};

export default {
    store,
    serviceName
};