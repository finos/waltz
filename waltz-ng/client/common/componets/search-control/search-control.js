import { initialiseData, invokeFunction } from "../../../common"
import _ from 'lodash';


const bindings = {
    onQuery: '<',
    minCharacters: '<',
    delay: '<',
    placeholderText: '@'
}


const template = require('./search-control.html');


const initialState = {
    minCharacters: 3,
    delay: 250,
    placeholderText: 'Search...',
    onQuery: query => console.log('default onQuery handler in search-control: ', query),
}


function controller($scope) {
    const vm = initialiseData(this, initialState);

    vm.options = {
        debounce: vm.delay
    };


    $scope.$watch('$ctrl.query', q => {
        if(_.isString(q) && (q.length >= vm.minCharacters || q === "")) {
            invokeFunction(vm.onQuery, q);
        }
    });

}


controller.$inject = ["$scope"];


const component = {
    bindings,
    template,
    controller
};


export default component;