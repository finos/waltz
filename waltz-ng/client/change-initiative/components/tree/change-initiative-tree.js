import template from "./change-initiative-tree.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRef, sameRef} from "../../../common/entity-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {
    buildHierarchies,
    findNode,
    getParents
} from "../../../common/hierarchy-utils";
import {kindToViewState} from "../../../common/link-utils";
import {entity} from "../../../common/services/enums/entity";

const bindings = {
    parentEntityRef: "<",
};

const initialState = {};

const fakeProgramme = {
    id: -2,
    parentId: -1,
    isFake: true,
    name: "Programme Placeholder",
    description: "Placeholder programme as there is no actual linked programme",
    kind: "CHANGE_INITIATIVE",
    changeInitiativeKind: "INITIATIVE"
};

const fakeInitiative = {
    id: -1,
    parentId: null,
    isFake: true,
    name: "Initiative Placeholder",
    description: "Placeholder programme as there is no actual linked initiative",
    kind: "CHANGE_INITIATIVE",
    changeInitiativeKind: "INITIATIVE"
};

const fakeParentsByChildKind = {
    "PROJECT": fakeProgramme,
    "PROGRAMME": fakeInitiative
};


function controller($state, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);
        serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                [ selector ])
            .then(r => {
                const initiatives = _
                    .chain(r.data)
                    .flatMap(d => {
                        const maybeFakeParent = d.parentId
                            ? null // fake parent not needed
                            : fakeParentsByChildKind[d.changeInitiativeKind]; // use fake parent for kind (except for top level items)

                        const enriched = Object.assign(
                            {},
                            d,
                            {
                                parentId: d.parentId || _.get(maybeFakeParent, "id", null),
                                isSelf: sameRef(d, vm.parentEntityRef, { skipChecks: true })
                            });
                        return [enriched, maybeFakeParent]

                    })
                    .compact()
                    .uniqBy(d => d.id)
                    .value();

                const hierarchy = buildHierarchies(initiatives, false);
                const byId = _.keyBy(initiatives, d => d.id);
                const self = findNode(hierarchy, vm.parentEntityRef.id);

                vm.expandedNodes = _.concat([self], getParents(self, n => byId[n.parentId]));
                vm.hierarchy = hierarchy; //switchToParentIds(hierarchy);
            });
    };

    vm.onSelectNavItem = (item) => {
        if (item.id === vm.parentEntityRef.id || item.isFake) {
            return; // nothing to do, user clicked on self
        }
        $state.go(
            kindToViewState(entity.CHANGE_INITIATIVE.key),
            { id: item.id });
    };
}


controller.$inject = [
    "$state",
    "ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzChangeInitiativeTree",
    component
};