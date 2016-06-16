export default (module) => {

    module
        .config(require('./routes'))
        .service('SourceDataRatingStore', require('./services/source-data-rating-store'))
        .directive('waltzSourceDataInfo', require('./directives/source-data-info'))
        .directive('waltzSourceDataOverlay', require('./directives/source-data-overlay'))
        .directive('waltzSourceDataSectionAddon', require('./directives/source-data-section-addon'));

};
