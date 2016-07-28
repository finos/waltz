import _ from "lodash";

const bindings = {

};


const template = require('./navbar-recently-viewed.html');


const initialState = {
    history: []
};


function controller(localStorageService) {
    const vm = _.defaultsDeep(this, initialState);

    vm.refreshHistory = () => vm.history = localStorageService.get('history_2') || [];
}


controller.$inject = ['localStorageService'];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: bindings,
    controllerAs: 'ctrl',
    controller,
    template
};


export default () => directive;