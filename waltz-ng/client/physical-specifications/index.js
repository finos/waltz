function setup(module) {

    module
        .config(require('./routes'))
        .component(
            'waltzPhysicalDataSection',
            require('./components/physical-data-section/physical-data-section'))
        .component(
            'waltzPhysicalSpecificationOverview',
            require('./components/overview/physical-specification-overview'))
        .component(
            'waltzPhysicalSpecificationConsumers',
            require('./components/specification-consumers/physical-specification-consumers'))
        .component(
            'waltzPhysicalSpecificationMentions',
            require('./components/mentions/physical-specification-mentions'))
        .service(
            'PhysicalSpecificationStore',
            require('./services/physical-specification-store'))
        ;
}


export default setup;
