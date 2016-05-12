const initData = {
    loadingStats: false,
    loadingFlows: false,
    flows: [],
    appIds: [],
    stats: {}
};


function service($q, dataFlowStore) {
    let data = initData;

    function initialise(appIds) {
        data = { ...initData };
        data.loadingStats = true;
        data.appIds = appIds;
        return dataFlowStore
            .calculateStats(appIds)
            .then(stats => {
                data.loadingStats = false;
                data.stats = stats;
                return data;
            });
    }


    function loadDetail() {
        if (data.flows.length > 0) {
            return $q.when(data);
        }

        data.loadingFlows = true;
        return dataFlowStore
            .findByAppIds(data.appIds)
            .then(flows => {
                data.loadingFlows = false;
                data.flows = flows;
                return data;
            });
    }

    return {
        initialise,
        loadDetail
    };
}


service.$inject = [
    '$q',
    'DataFlowDataStore'
];


export default service;