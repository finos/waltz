export default (module) => {

    module
        .config(require('./routes'))
        .service('SourceDataRatingStore', require('./services/source-data-rating-store'))
        .directive('waltzSourceDataInfo', require('./directives/source-data-info'))
        .directive('waltzSourceDataSectionAddon', require('./directives/source-data-section-addon'));

    module
        .component('waltzSourceDataOverlay', require('./components/source-data-overlay'));

};
