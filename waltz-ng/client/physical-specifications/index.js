import angular from 'angular';


function setup() {

    const module = angular.module('waltz.physical.specification', []);

    module
        .config(require('./routes'));

    module
        .component('waltzPhysicalDataSection', require('./components/physical-data-section/physical-data-section'))
        .component('waltzPhysicalSpecificationOverview', require('./components/overview/physical-specification-overview'))
        .component('waltzPhysicalSpecificationConsumers', require('./components/specification-consumers/physical-specification-consumers'))
        .component('waltzPhysicalSpecificationMentions', require('./components/mentions/physical-specification-mentions'));

    module
        .service('PhysicalSpecificationStore', require('./services/physical-specification-store'));

    return module.name;
}


export default setup;
