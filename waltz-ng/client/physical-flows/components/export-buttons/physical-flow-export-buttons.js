const bindings = {
    consumesCount: '<',
    producesCount: '<',
    unusedCount: '<',
    exportConsumes: '<',
    exportProduces: '<',
    exportUnused: '<'
};


const template = require('./physical-flow-export-buttons.html');


const component = {
    template,
    bindings
};

export default component;
