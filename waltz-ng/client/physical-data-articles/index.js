function setup(module) {

    module
        .config(require('./routes'))
        .component(
            'waltzPhysicalDataSection',
            require('./components/physical-data-section'))
        .component(
            'waltzPhysicalDataArticleOverview',
            require('./components/overview/physical-data-article-overview'))
        .component(
            'waltzPhysicalDataArticleConsumers',
            require('./components/article-consumers/physical-data-article-consumers'))
        .service(
            'PhysicalDataArticleStore',
            require('./service/physical-data-article-store'))
        ;
}


export default setup;
