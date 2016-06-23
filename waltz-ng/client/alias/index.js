function setup(module) {
    
    module
        .service('AliasStore', require('./services/alias-store'));

}

export default setup;
