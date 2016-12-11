import angular from 'angular';


export default () => {
    const module = angular.module('waltz.user.contribution', []);

    module
        .service('UserContributionStore', require('./services/store'));

    return module.name;
};
