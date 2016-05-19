
export default (module) => {

    module.service('SourceDataRatingStore', require('./services/source-data-rating-store'));
    module.directive('waltzSourceDataInfo', require('./directives/source-data-info'));
    module.directive('waltzSourceDataOverlay', require('./directives/source-data-overlay'));

};
