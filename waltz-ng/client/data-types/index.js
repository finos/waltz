


export default (module) => {

    module.config(require('./routes'));

    module.service('DataTypeStore', require('./services/data-type-store'));
    module.service('DataTypeService', require('./services/data-type-service'));

};
