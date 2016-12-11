import angular from 'angular';


export default () => {
    const module = angular.module('waltz.logical.flow.decorator', []);

    module
        .service('LogicalFlowDecoratorStore', require('./services/logical-flow-decorator-store'));

    return module.name;
};
