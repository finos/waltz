import {checkIsCreateLineageReportCommand} from '../../common/checks';


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/lineage-report`;

    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);

    const findBySpecificationId = (id) => $http
        .get(`${base}/specification/${id}`)
        .then(r => r.data);

    const findReportsContributedToBySpecificationId = (id) => $http
        .get(`${base}/specification/${id}/contributions`)
        .then(r => r.data);

    /**
     * Creates a new lineage report describing the
     * referenced specification.
     *
     * @param cmd : { name: <str>, specificationId: <num> }
     * @returns {Promise.<TResult>|*}
     */
    const create = (cmd) => {
        checkIsCreateLineageReportCommand(cmd);
        return $http
            .post(`${base}/update`, cmd)
            .then(r => r.data);
    };

    const update = (cmd) => {
        return $http
            .put(`${base}/update`, cmd)
            .then(r => r.data);
    };


    return {
        create,
        update,
        getById,
        findBySpecificationId,
        findReportsContributedToBySpecificationId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
