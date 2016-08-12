function service(dataTypeService) {

    const resolve = (params) => {
        return dataTypeService.loadDataTypes()
            .then(dataTypes => {
                const dataType = _.find(dataTypes, { code: params.code });
                if (dataType) {
                    return {id: dataType.id};
                } else {
                    throw `data type with code: ${params.code} not found`;
                }
            });

    };

    return {
        resolve
    };
}


service.$inject = [
    'DataTypeService'
];


export default service;