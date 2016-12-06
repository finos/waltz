import {checkIsApplicationIdSelector} from '../../common/checks';
import {notEmpty} from '../../common'

const initData = {
    loadingSummary: false,
    loadingCosts: false,
    costs: [],
    appIds: [],
    summary: []
};

function service($q,
                 assetCostStore)
{

    let data = initData;

    function initialise(selector) {
        checkIsApplicationIdSelector(selector);

        data = { ...initData };
        data.loadingSummary = true;
        data.options = selector;

        const topCostsPromise = assetCostStore
            .findTopAppCostsByAppIdSelector(selector);
        const totalCostPromise = assetCostStore
            .findTotalCostForAppSelector(selector);

        return $q
            .all([topCostsPromise, totalCostPromise])
            .then(([topCosts = [], total]) => {
                data.loadingSummary = false;
                data.summary = topCosts;
                data.total = total;
                return data;
            });
    }


    function loadDetail() {
        if (notEmpty(data.costs)) {
            return $q.when(data);
        }

        data.loadingDetail = true;
        return assetCostStore
            .findAppCostsByAppIdSelector(data.options)
            .then(costs => {
                data.loadingDetail = false;
                data.costs = costs;
                return Object.assign({}, data);
            });
    }



    return {
        initialise,
        loadDetail,
    };
}

service.$inject = [
    '$q',
    'AssetCostStore'
];


export default service;