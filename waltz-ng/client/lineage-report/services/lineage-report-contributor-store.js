function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/lineage-report-contributor`;

    // TODO: server side
    const findContributorsByReportId = (id) => $http
        .get(`${base}/report/${id}`)
        .then(r => r.data);


    const findContributorsByArticleId = id => $http
        .get(`${base}/physical-article/${id}`)
        .then(r => r.data);

    return {
        findContributorsByReportId,
        findContributorsByArticleId,
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
