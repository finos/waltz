
function setup(module) {
    module
        .config(require('./routes'))
        .service(
            'ChangeInitiativeStore',
            require('./services/change-initiative-store'))
        .directive(
            'waltzChangeInitiativeTable',
            require('./directives/change-initiative-table'))
        .directive(
            "waltzChangeInitiativeSelector",
            require("./directives/change-initiative-selector"));
}


export default setup;
