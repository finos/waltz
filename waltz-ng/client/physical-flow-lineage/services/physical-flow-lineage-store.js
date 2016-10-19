import {checkIsCreateLineageReportCommand} from '../../common/checks';


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow-lineage`;

    const findByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}`)
        .then(r => r.data);

    const findContributionsByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}/contributions`)
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
        findByPhysicalFlowId,
        findContributionsByPhysicalFlowId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
