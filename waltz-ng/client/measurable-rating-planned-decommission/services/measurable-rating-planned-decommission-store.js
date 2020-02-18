function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating-planned-decommission`;

    const findForEntityRef = (ref) => $http
        .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    return {
        findForEntityRef,
    };
}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingPlannedDecommissionStore";

export default {
    serviceName,
    store
};

export const MeasurableRatingPlannedDecommissionStore_API = {
    findForEntityRef: {
        serviceName,
        serviceFnName: "findForEntityRef",
        description: "finds all measurable decommission dates for a given entity"
    }
};