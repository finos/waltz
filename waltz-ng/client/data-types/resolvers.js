export function loadDataTypes(dataTypeService) {
    return dataTypeService.loadDataTypes();
}

loadDataTypes.$inject = ['DataTypeService'];


export function dataTypeIdResolver($q, $stateParams, dataTypeService) {
    if($stateParams.id) {
        const deferred = $q.defer();
        deferred.resolve($stateParams.id);
        return deferred.promise;
    } else {
        return dataTypeService.loadDataTypes()
            .then(dataTypes => {
                const dataType = _.find(dataTypes, { code: $stateParams.code });
                if (dataType) {
                    return dataType.id;
                } else {
                    throw `data type with code: ${$stateParams.code} not found`;
                }
            });
    }
}

dataTypeIdResolver.$inject = [ '$q', '$stateParams', 'DataTypeService'];