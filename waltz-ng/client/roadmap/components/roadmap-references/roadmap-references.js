import template from "./roadmap-references.html";
import {initialiseData} from "../../../common";


const bindings = {
    references: "<",
};


const initialState = {

};


function controller() {

    const vm = initialiseData(this, initialState);

}


controller.$inject = [];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzRoadmapReferences",
    component
};