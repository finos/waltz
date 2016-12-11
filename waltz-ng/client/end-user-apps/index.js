import angular from 'angular';


export default () => {

    const module = angular.module('waltz.end.user.apps', []);

    module
        .service('EndUserAppStore', require('./services/end-user-app-store'));

    return module.name;
};
