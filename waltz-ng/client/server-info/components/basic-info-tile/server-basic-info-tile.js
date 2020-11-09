import template from "./server-basic-info-tile.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {enrichServerStats} from "../../services/server-utilities";
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
                CORE_API.ServerInfoStore.findBasicStatsForSelector,
                [selector])
            .then(r => vm.stats = enrichServerStats(r.data));
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
    id: "waltzServerBasicInfoTile",
    component
};