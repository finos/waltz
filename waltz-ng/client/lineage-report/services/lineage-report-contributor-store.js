function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/lineage-report-contributor`;

    // TODO: server side
    const findContributorsByReportId = (id) => $http
        .get(`${base}/report/${id}`)
        .then(r => r.data);


    const findContributorsBySpecificationId = id => $http
        .get(`${base}/physical-specification/${id}`)
        .then(r => r.data);

    return {
        findContributorsByReportId,
        findContributorsBySpecificationId,
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
