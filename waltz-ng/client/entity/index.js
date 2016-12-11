import angular from 'angular';


export default () => {
    const module = angular.module('waltz.entity', []);

    module
        .component('waltzEntityHierarchyNavigator', require('./components/entity-hierarchy-navigator/entity-hierarchy-navigator'))
        .component('waltzImmediateHierarchyNavigator', require('./components/immediate-hierarchy-navigator/immediate-hierarchy-navigator'));

    return module.name;
};
