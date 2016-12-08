import _ from "lodash";

const bindings = {
    data: '<',
    config: '<',
    title: '@',
    subTitle: '@',
    icon: '@',
    selectedSegmentKey: '<'
};

const MAX_PIE_SEGMENTS = 5;

function controller() {

    const vm = this;

    const defaultOnSelect = (d) => {
        vm.selectedSegmentKey = d ? d.key : null;
    };

    const dataChanged = (data = []) => {
        vm.total = _.sumBy(data, 'count');

        if (data.length > MAX_PIE_SEGMENTS) {
            const sorted = _.sortBy(data, d => d.count * -1);

            const topData = _.take(sorted, MAX_PIE_SEGMENTS);
            const otherData = _.drop(sorted, MAX_PIE_SEGMENTS);
            const otherDatum = {
                key: 'Other',
                count : _.sumBy(otherData, "count")
            };

            vm.pieData = _.concat(topData, otherDatum);
        } else {
            vm.pieData = data;
        }

    };

    vm.$onChanges = (changes) => {
        dataChanged(vm.data);

        if (changes.config) {
            _.defaults(vm.config, { onSelect: defaultOnSelect });
        }
    };

    vm.toDisplayName = (k) => vm.config.labelProvider
        ? vm.config.labelProvider(k)
        : k;
}



const component = {
    template: require('./pie-table.html'),
    bindings,
    controller
};


export default component;
