import template from "./taxonomy-change-command-info.html";
import {initialiseData} from "../../../common";


const bindings = {
    change: "<"
};


const initialState = {};


function controller() {
    const vm = initialiseData(this, initialState);
}


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "taxonomyChangeCommandInfo"
}