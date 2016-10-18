const bindings = {
    spec: '<',
    owningApp: '<',
    organisationalUnit: '<'
};

const template = require('./physical-specification-overview.html');

const component = {
    template,
    bindings
};

export default component;