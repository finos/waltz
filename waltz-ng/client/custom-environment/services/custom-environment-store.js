
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/custom-environment`;

    const findAll = () =>
        $http
            .get(`${BASE}/all`)
            .then(r => r.data);

    const findUsagesByOwningEntityRef = (entityReference) =>
        $http
            .get(`${BASE}/usage/owning-entity/kind/${entityReference.kind}/id/${entityReference.id}`)
            .then(r => r.data);

    return {
        findAll,
        findUsagesByOwningEntityRef
    };
}

store.$inject = [
    "$http",
    "BaseApiUrl",
];

export const serviceName = "CustomEnvironmentStore";

export const CustomEnvironmentStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    findUsagesByOwningEntityRef: {
        serviceName,
        serviceFnName: "findUsagesByOwningEntityRef",
        description: "executes findUsagesByOwningEntityRef, [entityReference]"
    },
};

export default {
    serviceName,
    store
}
