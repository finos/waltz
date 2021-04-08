import template from "./auth-source-summary-list.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import AuthSourceDetail from "./AuthSourceDetail.svelte";
import AuthSourceCreate from "./AuthSourceEditor.svelte";
import {mode, selectedAuthSource} from "./editingAuthSources";


const bindings = {}

const initialState = {
    viewMode: "LIST"
}

function controller(serviceBroker, $scope){

    const loadAuthSources = ()  => {
        serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAll, [], {force: true})
            .then(r => vm.authSources = r.data);
    };

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        loadAuthSources();
        vm.authSummarySection = AuthSourceDetail;
        vm.createAuthSourceSection = AuthSourceCreate;
    };

    vm.onSelectAuthSource = (authSource) => {
        selectedAuthSource.set(authSource)
        mode.set("DETAIL");
    }

    vm.create = () => {
        selectedAuthSource.set({
            orgUnit: null,
            app: null,
            description: null,
            dataType: null, 
            rating: "SECONDARY"
        })
        mode.set("EDIT");
    }

    vm.cancel = () => {
        mode.set("LIST");
        selectedAuthSource.set(null);
    }

    vm.doSave = (cmd) => {
        return serviceBroker
            .execute(CORE_API.AuthSourcesStore.insert, [cmd])
            .then(() => {
                loadAuthSources();
                vm.cancel();
            });
    }

    vm.doUpdate = (cmd) => {
        return serviceBroker
            .execute(CORE_API.AuthSourcesStore.update, [cmd])
            .then(() => {
                loadAuthSources();
                mode.set("DETAIL");
            })
    }

    vm.doDelete = (id) => {
        if(confirm("Are you sure you want to delete this authority statement?")){
            return serviceBroker
                .execute(CORE_API.AuthSourcesStore.remove, [id])
                .then(() => {
                    loadAuthSources();
                    vm.cancel();
                })
        }
    }

    mode.subscribe(d => {
        $scope.$applyAsync(() => vm.viewMode = d)
    })

}

controller.$inject = [
    "ServiceBroker",
    "$scope"
]

const component = {
    template,
    controller,
    bindings
};


export default {
    id: "waltzAuthSourceSummaryList",
    component
}