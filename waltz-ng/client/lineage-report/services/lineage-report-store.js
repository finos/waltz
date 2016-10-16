import {checkIsCreateLineageReportCommand} from '../../common/checks';


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

    /**
     * Creates a new lineage report describing the
     * referenced article.
     *
     * @param cmd : { name: <str>, articleId: <num> }
     * @returns {Promise.<TResult>|*}
     */
    const create = (cmd) => {
        checkIsCreateLineageReportCommand(cmd);
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };

    return {
        create,
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
