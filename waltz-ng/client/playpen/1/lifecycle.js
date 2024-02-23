import _ from "lodash";
import {isRemoved} from "../../common/entity-utils";
import {initialiseData} from "../../common";
import {columnDef, withWidth} from "../../physical-flow/physical-flow-table-utilities";
import template from "../../physical-specifications/components/physical-data-section/physical-data-section.html";

const bindings = {

};


const initialState = {

};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        console.log("lifecycle onInit");
    }
    vm.$onDestroy = () => {
        console.log("lifecycle onDestroy");
    }
}

controller.$inject = ["$scope"];


const component = {
    template,
    bindings,
    controller
};


export default component;