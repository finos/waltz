function setup(module) {

    module
        .service(
            'PhysicalDataArticleStore',
            require('./service/physical-data-article-store'));

}


export default setup;
