import template from "./app-list.html";
import AppListPageHeader from "../../components/app-list-page-header/AppListPageHeader.svelte";
import {showGridSelector} from "../../../report-grid/components/svelte/report-grid-ui-service";
import namedSettings from "../../../system/named-settings";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {hierarchyQueryScope} from "../../../common/services/enums/hierarchy-query-scope";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";


const initialState = {
    AppListPageHeader,
    parentEntityRef: { kind: "ALL", id: 1},
    apps: []
};


function controller(serviceBroker) {
    const vm = Object.assign(this, initialState);

    const selector = mkSelectionOptions(
        vm.parentEntityRef,
        hierarchyQueryScope.CHILDREN.key,
        [entityLifecycleStatus.ACTIVE.key],
        vm.filters);

    serviceBroker
        .loadViewData(
            CORE_API.ApplicationStore.findBySelector,
            [ selector] )
        .then(r => vm.apps = r.data);

    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };
}



controller.$inject = [
    "ServiceBroker"
];


export default  {
    template,
    controller,
    controllerAs: "$ctrl"
};

