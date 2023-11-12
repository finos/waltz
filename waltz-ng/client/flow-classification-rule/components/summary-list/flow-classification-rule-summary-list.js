import template from "./flow-classification-rule-summary-list.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import FlowClassificationRuleDetail from "./FlowClassificationRuleDetail.svelte";
import FlowClassificationRuleEditor from "./FlowClassificationRuleEditor.svelte";
import {mode, selectedClassificationRule} from "./editingFlowClassificationRulesState";
import roles from "../../../user/system-roles";
import {mkRef} from "../../../common/entity-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {}

const initialState = {
    viewMode: "LIST",
    FlowClassificationRuleDetail,
    FlowClassificationRuleEditor,
    canEdit: false,
    view: null
};

function controller(serviceBroker, userService, $scope){

    const loadFlowClassificationRules = ()  => {
        serviceBroker
            .loadViewData(
                CORE_API.FlowClassificationRuleStore.view,
                [mkSelectionOptions(mkRef("ALL", 1))],
                {force: true})
            .then(r => vm.flowClassificationRules = r.data);
    };

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        loadFlowClassificationRules();
        userService
            .whoami()
            .then(user => vm.canEdit = userService.hasRole(user, roles.AUTHORITATIVE_SOURCE_EDITOR));
    };

    vm.$onDestroy =() => mode.set("LIST");

    vm.onSelectRule = (rule) => {
        selectedClassificationRule.set(rule)
        mode.set("DETAIL");
    }

    vm.create = () => {
        selectedClassificationRule.set({
            parentReference: null,
            subjectReference: null,
            description: null,
            dataType: null,
            rating: "SECONDARY"
        })
        mode.set("EDIT");
    }

    vm.cancel = () => {
        mode.set("LIST");
        selectedClassificationRule.set(null);
    }

    vm.doSave = (cmd) => {
        return serviceBroker
            .execute(CORE_API.FlowClassificationRuleStore.insert, [cmd])
            .then(() => {
                loadFlowClassificationRules();
                vm.cancel();
            });
    }

    vm.doUpdate = (cmd) => {
        return serviceBroker
            .execute(CORE_API.FlowClassificationRuleStore.update, [cmd])
            .then(() => {
                loadFlowClassificationRules();
                mode.set("DETAIL");
            });
    }

    vm.doDelete = (id) => {
        if(confirm("Are you sure you want to delete this flow classification rule?")){
            return serviceBroker
                .execute(CORE_API.FlowClassificationRuleStore.remove, [id])
                .then(() => {
                    loadFlowClassificationRules();
                    vm.cancel();
                });
        }
    }

    mode.subscribe(d => {
        $scope.$applyAsync(() => vm.viewMode = d)
    });

}

controller.$inject = [
    "ServiceBroker",
    "UserService",
    "$scope"
]

const component = {
    template,
    controller,
    bindings
};


export default {
    id: "waltzFlowClassificationRuleSummaryList",
    component
}