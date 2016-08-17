


export default (module) => {

    module.config(require('./routes'));

    module.service('DataTypeStore', require('./services/data-type-store'));
    module.service('DataTypeService', require('./services/data-type-service'));

    module.component('waltzDataTypeTree', require('./components/data-type-tree'));

};
