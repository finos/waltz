import template from './entity-relationship-overview.html';
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";


const bindings = {
    parentEntityRef: '<'
};

const initialState = [];


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadData() {
        const relationshipKindPromise = serviceBroker
            .loadAppData(CORE_API.RelationshipKindStore.findAll)
            .then(r => r.data);

        const relationshipPromise = serviceBroker
            .loadViewData(CORE_API.EntityRelationshipStore.getById, [vm.parentEntityRef.id])
            .then(r => r.data);


        return $q
            .all([relationshipKindPromise, relationshipPromise])
            .then(([relKinds, relationship]) =>  {

                const relationshipKind = _.find(relKinds,
                        k => k.code === relationship.relationship
                            && k.kindA === relationship.a.kind
                            && k.kindB === relationship.b.kind);

                vm.relationship = Object.assign({}, relationship, {relationshipKind: relationshipKind})
        })
    }

    vm.$onInit = () => {
        loadData();
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];

const component = {
    template,
    bindings,
    controller
};

export default {
    id: 'waltzEntityRelationshipOverview',
    component
}