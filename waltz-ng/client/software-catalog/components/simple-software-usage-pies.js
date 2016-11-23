import _ from "lodash";
import {maturityColorScale, variableScale} from "../../common/colors";


const bindings = {
    usages: '<',
    packages: '<'
};


const template = require('./simple-software-usage-pies.html');


const PIE_SIZE = 70;


function prepareStats(items = [], usages = []) {
    const usageCounts = _.countBy(usages, 'softwarePackageId');

    const countPieDataBy = (items = [], fn = (x => x)) =>
        _.chain(items)
            .groupBy(fn)
            .map((group, key) => {
                const calculatedCount = _.reduce(
                    group,
                    (acc, groupItem) => acc + usageCounts[groupItem.id] || 1,
                    0);
                return {
                    key,
                    count: calculatedCount
                };
            })
            .value();


    return {
        maturity: countPieDataBy(items, item => item.maturityStatus),
        vendor: countPieDataBy(items, item => item.vendor)
    };
}


function controller() {

    const vm = this;

    vm.pieConfig = {
        maturity: {
            size: PIE_SIZE,
            colorProvider: (d) => maturityColorScale(d.data.key)
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.data.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.packages, vm.usages);
    };


    vm.$onChanges = () => {
        if(vm.packages && vm.usages) {
            recalcPieData();
        }
    }

}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default component;
