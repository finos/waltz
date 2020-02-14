function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating-replacement`;

    const findForEntityRef = (ref) => $http
        .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    return {
        findForEntityRef,
    };
}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingReplacementStore";

export default {
    serviceName,
    store
};

export const MeasurableRatingReplacementStore_API = {
    findForEntityRef: {
        serviceName,
        serviceFnName: "findForEntityRef",
        description: "finds all replacement apps for a given entity"
    }
};