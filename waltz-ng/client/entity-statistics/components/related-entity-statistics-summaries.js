import _ from "lodash";


const bindings = {
    parentRef: '<',
    definitions: '<',
    summaries: '<'
};


const template = require('./related-entity-statistics-summaries.html');


function controller() {
    const vm = this;

    vm.$onChanges = (changes => {
        if (vm.summaries) {
            vm.summariesByDefinitionId = _.keyBy(vm.summaries, 'entityReference.id');
        }
    });

}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default component;
