import angular from 'angular';


export default () => {
    const module = angular.module('waltz.welcome', []);

    module
        .component('waltzRecentlyViewedSection', require('./components/recently-viewed-section/recently-viewed-section'));

    return module.name;
};
