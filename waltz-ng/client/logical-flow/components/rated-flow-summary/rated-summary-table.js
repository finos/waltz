import _ from 'lodash';
import {nest} from 'd3-collection';

const bindings = {
    exactSummaries: '<',
    childSummaries: '<',
    decoratorEntities: '<',
    onClick: '<'
};


const template = require('./rated-summary-table.html');


function nestByDecoratorThenRating(summaries = []) {
    return nest()
        .key(d => d.decoratorEntityReference.id)
        .key(d => d.rating)
        .rollup(ds => ds.length > 0
            ? ds[0].count
            : 0)
        .map(summaries);
}


function getRelevantDecorators(allDecorators = [], summaries = []) {
    const decoratorEntitiesById = _.keyBy(allDecorators, 'id');
    return _.chain(summaries)
        .map('decoratorEntityReference.id')
        .uniq()
        .map(id => decoratorEntitiesById[id])
        .filter(dec => dec != null)
        .sortBy('name')
        .value();
}


function setup(decoratorEntities = [], exactSummaries = [], childSummaries = []) {
    const maxCounts = nest()
        .key(d => d.decoratorEntityReference.id)
        .rollup(ds => _.sumBy(ds, "count"))
        .map(childSummaries);

    const totalCounts = nestByDecoratorThenRating(childSummaries);
    const directCounts = nestByDecoratorThenRating(exactSummaries);

    const result = {
        maxCounts,
        directCounts,
        totalCounts,
        decorators: getRelevantDecorators(decoratorEntities, childSummaries)
    };

    return result;
}


function controller() {
    const vm = this;

    const invokeClick = ($event, d) => {
        vm.onClick(d);
        if ($event) $event.stopPropagation();
    };

    vm.$onChanges = () => Object.assign(
        vm,
        setup(vm.decoratorEntities, vm.exactSummaries, vm.childSummaries));

    vm.columnClick = ($event, rating) => invokeClick($event, { type: 'COLUMN', rating });
    vm.rowClick = ($event, dataType) => invokeClick($event, { type: 'ROW', dataType });
    vm.cellClick = ($event, dataType, rating) => invokeClick($event, { type: 'CELL', dataType, rating });
}


const component = {
    bindings,
    template,
    controller
};


export default component;
