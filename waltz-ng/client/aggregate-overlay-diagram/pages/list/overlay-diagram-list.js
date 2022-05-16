import template from "./overlay-diagram-list.html";
import OverlayDiagramListView from "../../components/list-view/OverlayDiagramListView.svelte"
import {initialiseData} from "../../../common";
import {entity} from "../../../common/services/enums/entity";

const bindings = {}

const initialState = {
    OverlayDiagramListView
}


function controller(historyStore) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        historyStore.put(
            "Diagram List",
            entity.AGGREGATE_OVERLAY_DIAGRAM.key,
            "main.aggregate-overlay-diagram.list",
            {});
    }

}


controller.$inject = [
    "HistoryStore"
];

const component = {
    template,
    bindings,
    controller
}

export default {
    component,
    id: "waltzOverlayDiagramListView"
}