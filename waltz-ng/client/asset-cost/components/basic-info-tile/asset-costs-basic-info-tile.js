import template from "./asset-costs-basic-info-tile.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineDownwardsScopeForKind} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: "<",
    filters: "<?"
};


const initialState = {
    filters: {}
};



function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: determineDownwardsScopeForKind(vm.parentEntityRef.kind),
            filters: vm.filters
        };

        serviceBroker.loadAppData(CORE_API.CostKindStore.findAll)
            .then(r => r.data)
            .then(xs => _.find(xs, x => x.isDefault) || _.first(xs))
            .then(ck => serviceBroker
                .loadViewData(
                    CORE_API.CostStore.summariseByCostKindAndSelector,
                    [ ck.id, 'APPLICATION', selector ]))
            .then(r => vm.costSummary = r.data);

    };

}

controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzAssetCostsBasicInfoTile",
    component
};