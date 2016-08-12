import _ from "lodash";

export default (module) => {

    module.run(['$rootScope', '$state', 'DataTypesService', function($rootScope, $state, dataTypesService) {
        $rootScope.$on('$stateChangeStart', function(evt, to, params) {
            if (to.redirectTo) {
                evt.preventDefault();

                dataTypesService.loadDataTypes()
                    .then(dataTypes => {
                        const dataType = _.find(dataTypes, { code: params.code });
                        if (dataType) {
                            $state.go(to.redirectTo, {id: dataType.id});
                        } else {
                            throw `data type with code: ${params.code} not found`;
                        }
                    });
            }
        });
    }]);

    module.config(require('./routes'));

    module.service('DataTypesStore', require('./services/data-types-store'));
    module.service('DataTypesService', require('./services/data-types-service'));

    module.component('waltzDataTypeTree', require('./components/data-type-tree'));

};
