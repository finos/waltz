
export default (module) => {

    module.service('AssetCostStore', require('./services/asset-cost-store'));
    module.directive('waltzAssetCostTable', require('./directives/asset-cost-table'));

}