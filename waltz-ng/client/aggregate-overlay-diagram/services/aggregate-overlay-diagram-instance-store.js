function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/aggregate-overlay-diagram-instance`;

    const getById = (id) => $http
        .get(`${baseUrl}/id/${id}`)
        .then(d => d.data);

    return {
        getById
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "AggregateOverlayDiagramInstanceStore";

export default {
    serviceName,
    store
};

export const AggregateOverlayDiagramInstanceStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "getById [id]"
    }
}
