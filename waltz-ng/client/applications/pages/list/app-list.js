import template from "./app-list.html";
import AppListPageHeader from "../../components/app-list-page-header/AppListPageHeader.svelte";
import {showGridSelector} from "../../../report-grid/components/svelte/report-grid-ui-service";
import namedSettings from "../../../system/named-settings";


const initialState = {
    app: {},
    AppListPageHeader,
    parentEntityRef: { kind: "ALL", id: 1}
};


function controller(settingsService) {
    const vm = Object.assign(this, initialState);
    showGridSelector.set(false);
    settingsService
        .findOrDie(namedSettings.defaultAppGridId, `No default app grid id defined, should be under setting: ${namedSettings.defaultAppGridId}`)
        .then(d => vm.gridId = console.log(d) || d);
}



controller.$inject = [
    "SettingsService"
];


export default  {
    template,
    controller,
    controllerAs: "$ctrl"
};

