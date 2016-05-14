const initData = {
    loadingStats: false,
    loadingFlows: false,
    flows: [],
    options: {},
    stats: {}
};


function service($q, dataFlowStore) {
    let data = initData;

    function initialise(id, kind, scope = 'CHILDREN') {
        reset();
        data.loadingStats = true;
        data.options = {
            entityReference: { id, kind },
            scope
        };

        return dataFlowStore
            .calculateStats(data.options)
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
            .findByAppIdSelector(data.options)
            .then(flows => {
                data.loadingFlows = false;
                data.flows = flows;
                return data;
            });
    }


    function reset() {
        data = { ...initData };
    }


    return {
        initialise,
        loadDetail,
        reset
    };
}


service.$inject = [
    '$q',
    'DataFlowDataStore'
];


export default service;