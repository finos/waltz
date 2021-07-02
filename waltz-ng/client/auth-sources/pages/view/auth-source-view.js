import template from "./auth-source-view.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import AuthSourceOverview from "../../components/svelte/AuthSourceOverview.svelte"

const bindings = {
    
}

const initialState = {
    AuthSourceOverview,
    parentEntityRef: null
}

const addToHistory = (historyStore, id, name) => {
    if (! id) { return; }
    historyStore.put(
        `Auth source: ${name}`,
        "AUTHORITATIVE_SOURCE",
        "main.authoritative-source.view",
        { id });
};


function controller($q, $stateParams, serviceBroker, historyStore, dynamicSectionManager){

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

        const authSourceId = $stateParams.id;

        vm.parentEntityRef = {
            kind: "AUTHORITATIVE_SOURCE",
            id: authSourceId,
            name: "?"
        };

        dynamicSectionManager.initialise("AUTHORITATIVE_SOURCE");

        const dataTypesPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => _.keyBy(r.data, d => d.code));

        const authSourcePromise = serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.getById, [authSourceId])
            .then(r =>  r.data);

        $q.all([dataTypesPromise, authSourcePromise])
            .then(([datatypesByCode, authSource]) => {

                const datatype = datatypesByCode[authSource.dataType];
                const authSourceName =
                    authSource.applicationReference.name
                    + " - "
                    + _.get(datatype, "name", "unknown datatype");

                vm.parentEntityRef = {
                    kind: "AUTHORITATIVE_SOURCE",
                    id: authSourceId,
                    name: authSourceName
                };

                addToHistory(historyStore, authSource.id, authSourceName);
            });

    };
}

controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker",
    "HistoryStore",
    "DynamicSectionManager"
]


const component = {
    template,
    bindings,
    controller
}

export default {
    component,
    id: "waltzAuthSourceView"
}