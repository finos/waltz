import angular from 'angular';


export default () => {

    const module = angular.module('waltz.involvement.kind', []);

    module
        .service('InvolvementKindStore', require('./services/involvement-kind-store'))
        .service('InvolvementKindService', require('./services/involvement-kind-service'));

    return module.name;
};
