import {initialiseData} from "../../../common";

import template from "./key-people-sub-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {aggregatePeopleByKeyInvolvementKind} from "../../involvement-utils";
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";

const bindings = {
    parentEntityRef: "<",
};

const initialState = {
    keyPeople: []
};

function controller($q, serviceBroker, dynamicSectionManager) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const involvementPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findByEntityReference,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const peoplePromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findPeopleByEntityReference,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const keyInvolvementsPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementKindStore.findKeyInvolvementKindsByEntityKind,
                [ vm.parentEntityRef.kind ])
            .then(r => r.data);

        $q.all([involvementPromise, peoplePromise, keyInvolvementsPromise])
            .then(([involvements = [], people = [], keyInvolvementKinds = []]) => {
                vm.keyPeople = aggregatePeopleByKeyInvolvementKind(involvements, people, keyInvolvementKinds);
            });
    };

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            refresh();
        }
    };

    vm.onSelect = () => {
        dynamicSectionManager.activate(dynamicSections.involvedPeopleSection);
    };
}

controller.$inject = [
    "$q",
    "ServiceBroker",
    "DynamicSectionManager"
];


const component = {
    template,
    bindings,
    controller
};

export default {
    component,
    id: "waltzKeyPeopleSubSection"
};
