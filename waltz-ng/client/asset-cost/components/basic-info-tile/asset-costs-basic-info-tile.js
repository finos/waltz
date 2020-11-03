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

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTotalCostForAppSelector,
                [ selector ])
            .then(r => vm.totalCost = r.data);

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