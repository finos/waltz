import _ from "lodash";
import {variableScale} from "../common/colors";


const bindings = {
    config: '<',
    data: '<',
    headings: '<',
    selectedSegmentKey: '<'
};


const initialState = {
    total: 0,
    headings: []
};


const defaultConfig = {
    labelProvider: (d) => d.key,
    colorProvider: (d) => variableScale(d)
};


const defaultOnSelect = (d) => console.log('no pie-segment-table::on-select handler provided:', d);


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        if (changes.data) {
            vm.total = _.sumBy(vm.data, 'count');
        }

        if (changes.config) {
            vm.config = _.defaultsDeep(vm.config, defaultConfig);
            vm.rowSelected = (d, e) => {
                e.stopPropagation();
                (vm.config.onSelect || defaultOnSelect)(d);
            };
        }
    };

    vm.asPercentage = (d) => {
        const numerator = d.count || 0;
        const denominator = vm.total || 0;

        return denominator === 0
            ? '-'
            : Number((numerator / denominator) * 100).toFixed(1);
    };
}


const component = {
    bindings,
    controller,
    template: require('./pie-segment-table.html')
};


export default component;