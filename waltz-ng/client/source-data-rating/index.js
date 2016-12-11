import angular from 'angular';


export default () => {
    const module = angular.module('waltz.source.data.ratings', []);

    module
        .config(require('./routes'));

    module
        .service('SourceDataRatingStore', require('./services/source-data-rating-store'));

    module
        .directive('waltzSourceDataInfo', require('./directives/source-data-info'))
        .directive('waltzSourceDataOverlay', require('./directives/source-data-overlay'))
        .directive('waltzSourceDataSectionAddon', require('./directives/source-data-section-addon'));

    return module.name;
};
