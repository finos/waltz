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
    keyPeople: [],
    visibility: {
        editor: false
    }
};


function getPeopleWithRoleNames(involvements = [], displayNameService) {
    return _.chain(involvements)
        .map(inv => ({
            person: inv.person,
            rolesDisplayName: displayNameService.lookup("involvementKind", inv.involvement.kindId)
        }))
        .sortBy("person.displayName")
        .value();
}

function controller($q, displayNameService, serviceBroker, dynamicSectionManager) {

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

        $q.all([involvementPromise, peoplePromise])
            .then(([involvements = [], people = []]) => {
                const aggInvolvements = getPeopleWithInvolvements(involvements, people);
                vm.keyPeople = getPeopleWithRoleNames(aggInvolvements, displayNameService);
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
    "DisplayNameService",
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
