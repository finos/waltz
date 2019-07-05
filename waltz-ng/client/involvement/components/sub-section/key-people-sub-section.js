import {initialiseData} from "../../../common";

import template from "./key-people-sub-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {getPeopleWithInvolvements} from "../../involvement-utils";
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";

const bindings = {
    parentEntityRef: "<",
};

const initialState = {
    keyPeople: []
};

function getPeopleWithRoleNames(involvements = [], keyInvolvementKind = []) {
    const peopleGroupByKindId =
        _.chain(involvements)
            .groupBy(inv => inv.involvement.kindId);

    return _.chain(keyInvolvementKind)
        .map(kind => ({
            rolesDisplayName: kind.name,
            person: peopleGroupByKindId.get(kind.id).value()
        }))
        .sortBy("rolesDisplayName")
        .value();
}


function controller($q, serviceBroker, dynamicSectionManager) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const involvementPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findByEntityReferenceAndKeyInvolvements,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => r.data);

        const peoplePromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findPeopleByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => r.data);

        const keyInvolvementsPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementKindStore.findKeyInvolvementKindsByEntityKind,
                [ vm.parentEntityRef.kind ],
                { force: true })
            .then(r => r.data);

        $q.all([involvementPromise, peoplePromise, keyInvolvementsPromise])
            .then(([involvements = [], people = [], keyInvolvements = []]) => {
                const aggInvolvements = getPeopleWithInvolvements(involvements, people);
                vm.keyPeople = getPeopleWithRoleNames(aggInvolvements, keyInvolvements);
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
