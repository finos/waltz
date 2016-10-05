function setup(module) {

    module
        .component(
            'waltzPhysicalDataSection',
            require('./components/physical-data-section'))
        .service(
            'PhysicalDataArticleStore',
            require('./service/physical-data-article-store'))
        ;

}


export default setup;
