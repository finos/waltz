


export default (module) => {

    module.config(require('./routes'));

    module.service('DataTypeStore', require('./services/data-type-store'));
    module.service('DataTypeService', require('./services/data-type-service'));

    module.service('DataTypeCodeIdResolverService', require('./services/data-type-code-id-resolver-service'));

};
