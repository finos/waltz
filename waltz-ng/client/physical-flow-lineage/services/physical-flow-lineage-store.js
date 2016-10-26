
function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow-lineage`;

    const findByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}`)
        .then(r => r.data);

    const findContributionsByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}/contributions`)
        .then(r => r.data);

    const findAllContributions = () => $http
        .get(`${base}/reports`)
        .then(r => r.data);

    const removeContribution = (describedFlowId, contributorFlowId) => $http
        .delete(`${base}/physical-flow/${describedFlowId}/contributions/${contributorFlowId}`)
        .then(r => r.data);

    const addContribution = (describedFlowId, contributorFlowId) => $http
        .put(`${base}/physical-flow/${describedFlowId}/contributions`, contributorFlowId)
        .then(r => r.data);

    return {
        removeContribution,
        addContribution,
        findByPhysicalFlowId,
        findContributionsByPhysicalFlowId,
        findAllContributions
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
