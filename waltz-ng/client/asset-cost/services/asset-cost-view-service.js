import { checkIsApplicationIdSelector } from '../../common/checks';

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

    function initialise(selector, year) {
        checkIsApplicationIdSelector(selector);

        data = { ...initData };
        data.loadingStats = true;
        data.options = selector;
        return assetCostStore
            .findStatsByAppIds(data.options, year)
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