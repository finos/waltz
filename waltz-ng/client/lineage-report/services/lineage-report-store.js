function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/lineage-report`;

    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);

    const findByPhysicalArticleId = (id) => $http
        .get(`${base}/physical-article/${id}`)
        .then(r => r.data);

    const findReportsContributedToByArticleId = (id) => $http
        .get(`${base}/physical-article/${id}/contributions`)
        .then(r => r.data);


    return {
        getById,
        findByPhysicalArticleId,
        findReportsContributedToByArticleId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
