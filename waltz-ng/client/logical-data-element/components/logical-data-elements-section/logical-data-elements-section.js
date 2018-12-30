import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import template from "./logical-data-elements-section.html";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    dataTypes: [],
    dataElements: []
};


function prepareData(dataTypes = [], dataElements = []) {
    const typesById = _.keyBy(dataTypes, "id");

    return _.chain(dataElements)
        .groupBy("parentDataTypeId")
        .map((ldes, typeId) => {
            return {
                dataType: typesById[typeId],
                dataElementRefs: _.sortBy(ldes, "name")
            }
        })
        .value();
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const dtPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const ldePromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalDataElementStore.findBySelector,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.dataElements = r.data);

        dtPromise
            .then(() => ldePromise)
            .then(() => vm.groupedDataElements = prepareData(vm.dataTypes, vm.dataElements));
    };

    vm.$onChanges = () => {
        vm.groupedDataElements = prepareData(vm.dataTypes, vm.dataElements);
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzLogicalDataElementsSection";


export default {
    component,
    id
};
