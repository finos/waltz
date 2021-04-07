import template from "./auth-source-summary-list.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import AuthSourceDetail from "./AuthSourceDetail.svelte";
import AuthSourceCreate from "./AuthSourceCreate.svelte";
import {selectedAuthSource} from "./editingAuthSources";

import _ from "lodash";


const bindings = {}

const initialState = {
    visibility: {
        list: true,
        detail: false,
        create: false
    }
}

function controller(serviceBroker, $scope){

    const loadAuthSources = ()  => {
        serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAll)
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
    }

    vm.create = () => {
        vm.visibility.list = false;
        vm.visibility.detail = false;
        vm.visibility.create = true;
    }

    vm.cancel = () => {
        selectedAuthSource.set(null);
        vm.visibility.list = true;
        vm.visibility.detail = false;
        vm.visibility.create = false;
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
                loadAuthSources()
            })
    }

    selectedAuthSource.subscribe(d => {
        $scope.$applyAsync(() => {
            vm.visibility.detail = !_.isNil(d);
            vm.visibility.list = _.isNil(d);
        });
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