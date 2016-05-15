const initData = {
    loadingStats: false,
    loadingCosts: false,
    costs: [],
    appIds: [],
    stats: {}
};

function service($q,
                 assetCostStore)
{

    let data = initData;

    function initialise(id, kind, scope = 'CHILDREN', year = 2015) {
        data = { ...initData };
        data.loadingStats = true;
        data.options = {
            year,
            idSelectionOptions: {
                scope,
                entityReference: { id, kind }
            }
        };
        return assetCostStore
            .findStatsByAppIds(data.options)
            .then(stats => {
                data.loadingStats = false;
                data.stats = stats;
                return data;
            });
    }


    function loadDetail() {
        if (data.costs.length > 0) {
            return $q.when(data);
        }

        data.loadingDetail = true;
        return assetCostStore
            .findAppCostsByAppIds(data.options)
            .then(costs => {
                data.loadingDetail = false;
                data.costs = costs;
                return data;
            });
    }


    function selectBucket(bucket) {
        data.selectedBucket = bucket;
        return $q.when(data);
    }


    return {
        initialise,
        loadDetail,
        selectBucket
    };
}

service.$inject = [
    '$q',
    'AssetCostStore'
];


export default service;