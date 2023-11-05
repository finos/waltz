
export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/involvement-view`;

    const findAllInvolvementsByEmployeeId = (employeeId) => $http
        .get(`${BASE}/employee/${employeeId}`)
        .then(result => result.data);

    const findAllInvolvementsForEntityByDirection = (ref) => $http
        .get(`${BASE}/entity/kind/${ref.kind}/id/${ref.id}/all-by-direction`)
        .then(result => result.data);

    const findKeyInvolvementsForEntity = (ref) => $http
        .get(`${BASE}/entity/kind/${ref.kind}/id/${ref.id}/key`)
        .then(result => result.data);

    return {
        findAllInvolvementsByEmployeeId,
        findAllInvolvementsForEntityByDirection,
        findKeyInvolvementsForEntity
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
    },
    findAllInvolvementsForEntityByDirection: {
        serviceName,
        serviceFnName: "findAllInvolvementsForEntityByDirection",
        description: "find all direct, ancestor and descendent involvements for an entity"
    },
    findKeyInvolvementsForEntity: {
        serviceName,
        serviceFnName: "findKeyInvolvementsForEntity",
        description: "find all key involvements for an entity"
    }
};

export default {
    store,
    serviceName
};