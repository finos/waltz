import angular from 'angular';


export default () => {
    const module = angular.module('waltz.tour', []);

    module
        .service('TourService', require('./services/tour-service'))
        .service('TourStore', require('./services/tour-store'));

    return module.name;
}