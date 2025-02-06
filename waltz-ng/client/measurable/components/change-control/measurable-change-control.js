/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import template from "./measurable-change-control.html";
import { initialiseData } from "../../../common";
import { CORE_API } from "../../../common/services/core-api-utils";
import { toEntityRef } from "../../../common/entity-utils";
import { determineColorOfSubmitButton } from "../../../common/severity-utils";
import { buildHierarchies } from "../../../common/hierarchy-utils";
import { displayError } from "../../../common/error-utils";
import { getValidationErrorIfMeasurableChangeIsNotValid } from "../../measurable-change-utils";
import toasts from "../../../svelte-stores/toast-store";
import ReorderMeasurables from "./ReorderMeasurables.svelte";


const modes = {
    MENU: "MENU",
    OPERATION: "OPERATION",
};



const bindings = {
    measurable: "<",
    siblings: "<",
    changeDomain: "<",
    onSubmitChange: "<",
    pendingChanges: "<"
};


const rootNode = {
    id: "", // setting to empty string so it can be deserialized in java
    name: "[Root]"
};


const initialState = {
    commandParams: {},
    modes: modes,
    mode: modes.MENU,
    preview: null,
    parent: null,
    root: rootNode,
    selectedOperation: null,
    submitDisabled: true,
    ReorderMeasurables
};



function controller($scope,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);

    function mkCmd(params = {}) {

        const paramProcessor = vm.selectedOperation.paramProcessor || _.identity;
        const processedParam = paramProcessor(params);

        return {
            changeType: vm.selectedOperation.code,
            changeDomain: toEntityRef(vm.changeDomain),
            primaryReference: toEntityRef(vm.measurable),
            params: processedParam,
            createdBy: vm.userName,
            lastUpdatedBy: vm.userName
        };
    }

    function mkUpdCmd() {
        return mkCmd(vm.commandParams);
    }

    function mkPreviewCmd() {
        return mkCmd(vm.commandParams);
    }

    function calcPreview() {
        return serviceBroker
            .execute(CORE_API.TaxonomyManagementStore.preview, [ mkPreviewCmd() ])
            .then(r => {
                const preview = r.data;
                vm.preview = preview;
                const severities = _.map(preview.impacts, "severity");
                vm.submitButtonClass = determineColorOfSubmitButton(severities);
            });
    }


    function resetForm(params) {
        vm.commandParams = Object.assign({}, params);
        vm.submitDisabled = true;
    }


    const updateMenu = {
        name: "Update",
        description: `These are operations modify an existing taxonomy element.  Care must be taken
                to prevent inadvertently altering the <em>meaning</em> of nodes.  The operations
                will not result in data loss.`,
        options: [
            {
                name: "Name",
                code: "UPDATE_NAME",
                title: "Update name",
                description: `The name of the taxonomy item may be changed, however care should be
                    taken to prevent inadvertently altering the <em>meaning</em> of the item`,
                icon: "edit",
                onShow: () => {
                    resetForm({ name: vm.measurable.name });
                    calcPreview();
                },
                onChange: () => {
                    vm.submitDisabled = vm.commandParams.name === vm.measurable.name;
                },
                paramProcessor: (d) => Object.assign(d, {originalValue: vm.measurable.name})
            }, {
                name: "Description",
                code: "UPDATE_DESCRIPTION",
                icon: "edit",
                description: `The description of the taxonomy item may be changed, however care should be
                    taken to prevent inadvertently altering the <em>meaning</em> of the item.`,
                onShow: () => {
                    resetForm({ description: vm.measurable.description });
                    calcPreview();
                },
                onChange: () => {
                    vm.submitDisabled = vm.commandParams.description === vm.measurable.description;
                },
                paramProcessor: (d) => Object.assign(d, {originalValue: vm.measurable.description})
            }, {
                name: "Concrete",
                code: "UPDATE_CONCRETENESS",
                title: "Update Concrete Flag",
                description: `The concrete flag is used to determine whether applications may
                    use this taxonomy item to describe themselves via ratings.  Typically
                    higher level <em>grouping</em> items are non-concrete as they are not
                    specific enough to accurately describe the portfolio.`,
                icon: "edit",
                onShow: () => {
                    resetForm({ concrete: !vm.measurable.concrete });
                    vm.submitDisabled = false;
                    calcPreview();
                },
                paramProcessor: (d) => Object.assign(d, {originalValue: vm.measurable.concrete})
            }, {
                name: "External Id",
                code: "UPDATE_EXTERNAL_ID",
                icon: "edit",
                description: `The external identifier of the taxonomy item may be changed, however care should be
                    taken to prevent potentially breaking downstream consumers / reporting systems that rely
                    on the identifier.`,
                onShow: () => {
                    resetForm({ externalId: vm.measurable.externalId });
                    calcPreview();
                },
                onChange: () => {
                    vm.submitDisabled = vm.commandParams.externalId === vm.measurable.externalId;
                },
                paramProcessor: (d) => Object.assign(d, {originalValue: vm.measurable.externalId})
            }, {
                name: "Move",
                code: "MOVE",
                icon: "arrows",
                description: `Taxonomy items can be moved from one part of the tree to another.  Be aware that
                    child nodes <em>will</em> move with their parent. Also note that this operation may affect the
                    <em>cumulative</em> values for the impacted branches.`,
                onChange: (dest) => {
                    if (dest.id === vm.parent.id) {
                        toasts.warning("Same parent selected, ignoring....");
                        vm.commandParams.destination = null;
                        vm.submitDisabled = true;
                    } else {
                        vm.commandParams.destination = dest;
                        vm.submitDisabled = false;
                    }
                },
                onReset: () => {
                    vm.commandParams.destination = null;
                    vm.submitDisabled = true;
                },
                onShow: () => {
                    resetForm();
                },
                paramProcessor: (d) => ({
                    originalValue: vm.parent.name,
                    destinationId: d.destination.id,
                    destinationName: d.destination.name
                })
            }, {
                name: "Reorder Siblings",
                code: "REORDER_SIBLINGS",
                icon: "random",
                description: `Taxonomy items are naturally sorted alphabetically.  You may override this sorting to
                    provide a closer alignment with the underlying domain`,
                onChange: (newList) => {
                    $scope.$applyAsync(() => {
                        vm.submitDisabled = false;
                        vm.commandParams.list = newList;
                    });
                },
                onReset: () => {
                    vm.submitDisabled = true;
                    vm.commandParams.list = [];
                },
                onShow: () => {
                    resetForm();
                },
                paramProcessor: (commandParams) => {
                    return {
                        originalValue: JSON.stringify(_.map(vm.siblings, d => d.name)),
                        list: JSON.stringify(_.map(commandParams.list, d => d.id)),
                        listAsNames: JSON.stringify(_.map(commandParams.list, d => d.name))
                    }
                }
            }
        ]
    };

    const creationMenu = {
        name: "Create",
        description: `These operations introduce new elements in the taxonomy. They will
                <strong>not</strong> result in data loss.`,
        color: "#0b8829",
        options: [
            {
                name: "Add Child",
                code: "ADD_CHILD",
                icon: "plus-circle",
                description: "Adds a new element to the taxonomy underneath the currently selected item.",
                onShow: () => {
                    resetForm({ concrete: true });
                    calcPreview();
                },
                onToggleConcrete: () => vm.commandParams.concrete = ! vm.commandParams.concrete,
                onChange: () => {
                    const required = [vm.commandParams.name, vm.commandParams.externalId];
                    vm.submitDisabled = _.some(required, _.isEmpty);
                }
            }, {
                name: "Add Peer",
                code: "ADD_PEER",
                icon: "plus-circle",
                description: "Adds a new element to the taxonomy next to the currently selected item.",
                onShow: () => {
                    resetForm({ concrete: true });
                    calcPreview();
                },
                onToggleConcrete: () => vm.commandParams.concrete = ! vm.commandParams.concrete,
                onChange: () => {
                    const required = [vm.commandParams.name, vm.commandParams.externalId];
                    vm.submitDisabled = _.some(required, _.isEmpty);
                }
            }
        ]
    };

    const destructiveMenu = {
        name: "Destructive",
        description: `These operations <strong>will</strong> potentially result in data loss and
                should be used with care`,
        color: "#b40400",
        options: [
            {
                name: "Merge",
                code: "MERGE",
                icon: "code-fork",
                description: "Merges this item with another and all of it's children will be migrated",
                onShow: () => {
                    resetForm();
                    calcPreview();
                },
                paramProcessor: (d) => _.isEmpty(d)
                    ? {}
                    : ({
                        targetId: d.target.id,
                        targetName: d.target.name
                    }),
                onReset: () => {
                    vm.commandParams.target = null;
                    vm.submitDisabled = true;
                },
                onChange: (target) => {
                    if (target === null) {
                        toasts.warning("Must have selected a arget to merge, ignoring....");
                        vm.commandParams.target = null;
                        vm.submitDisabled = true;
                    } else if (target.id === vm.measurable.id) {
                        toasts.warning("Cannot merge onto yourself, ignoring....");
                        vm.commandParams.target = null;
                        vm.submitDisabled = true;
                    } else if (vm.measurable.concrete && !target.concrete) {
                        toasts.warning("Cannot migrate to a non-concrete node, ignoring....");
                        vm.commandParams.target = null;
                        vm.submitDisabled = true;
                    } else {
                        vm.commandParams.target = target;
                        vm.submitDisabled = false;
                        calcPreview();
                    }
                }
            },
            {
                name: "Remove",
                code: "REMOVE",
                icon: "trash",
                description: "Removes the item and all of it's children from the taxonomy",
                onShow: () => {
                    resetForm();
                    calcPreview();
                    vm.submitDisabled = false;
                },
            }
        ]
    };

    vm.menus = [
        creationMenu,
        updateMenu,
        destructiveMenu
    ];

    // --- boot

    vm.$onInit = () => {
        userService
            .whoami()
            .then(u => vm.userName = u.userName);

        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => {
                const measurables = _.filter(r.data, { categoryId: vm.changeDomain.id});
                vm.parent = _.find(measurables, { id: vm.measurable.parentId}) || vm.root;
                vm.tree = buildHierarchies(measurables, false);
            });
    };

    vm.$onChanges = (c) => {
        if (c.measurable) {
            vm.onDismiss();
        }
    };


    // --- interact

    vm.toTemplateName = (op) => `wmcc/${op.code}.html`;

    vm.onDismiss = () => {
        vm.mode = modes.MENU;
        vm.preview = null;
    };

    vm.onSelectOperation = (op) => {
        vm.mode = modes.OPERATION;
        vm.selectedOperation = op;
        return _.isFunction(op.onShow)
            ? op.onShow()
            : Promise.resolve();
    };

    vm.onSubmit = () => {
        if (vm.submitDisabled) return;
        const cmd = mkUpdCmd();

        const errorMessage =
            getValidationErrorIfMeasurableChangeIsNotValid(cmd, vm.measurable, vm.pendingChanges);

        if (errorMessage != null) {
            toasts.warning(errorMessage);
            vm.commandParams.destination = null;
            vm.submitDisabled = true;
            return;
        }

        vm.onSubmitChange(cmd)
            .then(vm.onDismiss)
            .catch(e => displayError("Error when submitting command", e));
    };
}


controller.$inject = [
    "$scope",
    "ServiceBroker",
    "UserService"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzMeasurableChangeControl",
    component
}
