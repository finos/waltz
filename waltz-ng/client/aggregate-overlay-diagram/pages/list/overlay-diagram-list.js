import template from "./overlay-diagram-list.html";
import OverlayDiagramListView from "../../components/list-view/OverlayDiagramListView.svelte"
import {initialiseData} from "../../../common";

const bindings = {}

const initialState = {
    OverlayDiagramListView
}


function controller() {

    const vm = initialiseData(this, initialState);

}


controller.$inject = [];

const component = {
    template,
    bindings,
    controller
}

export default {
    component,
    id: "waltzOverlayDiagramListView"
}