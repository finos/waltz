import _ from 'lodash';


const BINDINGS = {
    clear: '=',
    search: '='
};


function controller() {

    const vm = this;

    vm.doSearch = (q) => {
        if (_.isFunction(vm.search)) vm.search(q);
    };


    vm.clearSearch = () => {
        if (_.isFunction(vm.clear)) vm.clear();
        vm.qry = '';
    };
}


controller.$inject = [];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./search.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
