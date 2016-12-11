import angular from 'angular';


export default () => {
    const module = angular.module('waltz.common', []);

    require('./components')(module);
    require('./filters')(module);
    require('./services')(module);

    return module.name;
}