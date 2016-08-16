export function loadDataTypes(dataTypeService) {
    return dataTypeService.loadDataTypes();
}

loadDataTypes.$inject = ['DataTypeService'];


export function dataTypeByIdResolver($stateParams, dataTypeService) {
    if(!$stateParams.id)
        throw "'id' not found in stateParams";

    return dataTypeService
        .loadDataTypes()
        .then(dataTypes => {
            const dataType = _.find(dataTypes, { id: $stateParams.id});
            if (dataType) {
                return dataType;
            } else {
                throw `data type with id: ${$stateParams.id} not found`;
            }
        });
}

dataTypeByIdResolver.$inject = ['$stateParams', 'DataTypeService'];


export function dataTypeByCodeResolver($stateParams, dataTypeService) {
    if(!$stateParams.code)
        throw "'code' not found in stateParams";

    return dataTypeService
        .loadDataTypes()
        .then(dataTypes => {
            const dataType = _.find(dataTypes, { code: $stateParams.code});
            if (dataType) {
                return dataType;
            } else {
                throw `data type with code: ${$stateParams.code} not found`;
            }
        });
}

dataTypeByCodeResolver.$inject = ['$stateParams', 'DataTypeService'];