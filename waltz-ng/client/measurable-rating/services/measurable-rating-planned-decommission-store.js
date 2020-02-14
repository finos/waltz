function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating-planned-decommission`;

    const findForEntityRef = (ref) => $http
        .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    const save = (ref, measurableId, dateChange) => $http
        .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/MEASURABLE/${measurableId}`, dateChange)
        .then(d => d.data);

    const remove = (id) => $http
        .delete(`${baseUrl}/id/${id}`)
        .then(d => d.data);

    return {
        findForEntityRef,
        save,
        remove
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
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "saves decommission date,  [ref, measurableId, fieldChange{newVal, oldVal}]"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "removes decommission date and any replacement applications"
    }
};