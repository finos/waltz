
export default (module) => {

    module
        .service('AssetCostStore', require('./services/asset-cost-store'))
        .service('AssetCostViewService', require('./services/asset-cost-view-service'));

    module
        .directive('waltzAssetCostTable', require('./directives/asset-cost-table'))
        .directive('waltzAssetCostsSection', require('./directives/asset-costs-section'));

    module
        .component('waltzAssetCostsGraph', require('./components/asset-costs-graph'));

}