import _ from "lodash";
import {tallyBy} from "../../common/tally-utils";
import {lifecyclePhaseColorScale, criticalityColorScale, variableScale} from "../../common/colors";
import {
    criticalityDisplayNames,
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames
} from "../../common/services/display_names";


const bindings = {
    apps: '<',
    endUserApps: '<'
};


const initialState = {
    visibility: {
        apps: false,
        endUserApps: false
    }
};


const PIE_SIZE = 70;


const defaultLabelProvider = (d) => d.key;
const lifecycleLabelProvider = d => lifecyclePhaseDisplayNames[d.key] || d.key;
const criticalityLabelProvider = d => d ? (criticalityDisplayNames[d.key] || d.key) : d;
const applicationKindLabelProvider = d => applicationKindDisplayNames[d.key] || d.key;

const randomColorProvider = d => variableScale(d.data.key);
const lifecycleColorProvider = d => lifecyclePhaseColorScale(d.data.key);
const criticalityColorProvider = d => criticalityColorScale(d.data.key);


function mkChartData(data,
                     groupingField,
                     size,
                     colorProvider = randomColorProvider,
                     labelProvider = defaultLabelProvider) {
    return {
        config: {
            colorProvider,
            labelProvider,
            size
        },
        data: tallyBy(data, groupingField)
    };
}


function mkCharts(apps = [], endUserApps = []) {
    return {
        apps: {
            byLifecyclePhase: mkChartData(
                apps,
                'lifecyclePhase',
                PIE_SIZE,
                lifecycleColorProvider,
                lifecycleLabelProvider),
            byKind: mkChartData(
                apps,
                'kind',
                PIE_SIZE,
                randomColorProvider,
                applicationKindLabelProvider)
        },
        endUserApps: {
            byLifecyclePhase: mkChartData(
                endUserApps,
                'lifecyclePhase',
                PIE_SIZE,
                lifecycleColorProvider,
                lifecycleLabelProvider),
            byKind: mkChartData(
                endUserApps,
                'platform',
                PIE_SIZE),
            byRiskRating: mkChartData(
                endUserApps,
                'riskRating',
                PIE_SIZE,
                criticalityColorProvider,
                criticalityLabelProvider())
        }
    };
}


function calcVisibility(apps = [], endUserApps = []) {
    return {
        apps: apps.length > 0,
        endUserApps: endUserApps.length > 0
    };
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = () => {
        vm.charts = mkCharts(vm.apps, vm.endUserApps);
        vm.visibility = calcVisibility(vm.apps, vm.endUserApps);
    };
}


const component = {
    template: require('./app-summary.html'),
    controller,
    bindings
};


export default component;
