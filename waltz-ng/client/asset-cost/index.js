
export default (module) => {

    module
        .service('AssetCostStore', require('./services/asset-cost-store'))
        .service('AssetCostViewService', require('./services/asset-cost-view-service'));

    module
        .component('waltzAssetCostsGraph', require('./components/asset-costs-graph'))
        .component('waltzAssetCostsSection', require('./components/asset-costs-section'));

    module
        .directive('waltzAssetCostTable', require('./directives/asset-cost-table'))

}