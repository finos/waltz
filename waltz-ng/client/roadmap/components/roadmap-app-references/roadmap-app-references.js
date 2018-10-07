import template from "./roadmap-app-references.html";
import {initialiseData} from "../../../common";


const bindings = {
    references: "<",
};


const initialState = {

};


function controller() {

    const vm = initialiseData(this, initialState);

    global.vm = vm;

}


controller.$inject = [];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzRoadmapAppReferences",
    component
};