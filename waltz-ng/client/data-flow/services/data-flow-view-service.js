const initData = {
    loadingStats: false,
    loadingFlows: false,
    decorators: [],
    flows: [],
    options: {},
    stats: {}
};


function service($q,
                 dataFlowStore,
                 dataFlowDecoratorStore) {
    let data = initData;

    function initialise(id, kind, scope = 'CHILDREN') {
        reset();
        data.loadingStats = true;

        data.options = _.isObject(id)
            ? id
            : { entityReference: { id, kind }, scope };

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


        const flowPromise = dataFlowStore
            .findByAppIdSelector(data.options)
            .then(flows => data.flows = flows);

        const decoratorPromise = dataFlowDecoratorStore
            .findBySelectorAndKind(data.options, 'DATA_TYPE')
            .then(decorators => data.decorators = decorators);

        return $q
            .all([flowPromise, decoratorPromise])
            .then(() => data.loadingFlows = false)
            .then(() => data);
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
    'DataFlowDataStore',
    'DataFlowDecoratorStore'
];


export default service;