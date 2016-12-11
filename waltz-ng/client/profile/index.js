import angular from 'angular';


export default () => {
    const module = angular.module('waltz.profile', []);

    module
        .config(require('./routes'));

    return module.name;
}