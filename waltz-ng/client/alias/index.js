function setup() {

    const module = angular.module('waltz.alias', []);

    module
        .service('AliasStore', require('./services/alias-store'));

    return module.name;
}

export default setup;
