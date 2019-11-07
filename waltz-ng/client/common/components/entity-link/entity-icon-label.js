/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../../../common";
import template from "./entity-icon-label.html";
import {CORE_API} from "../../services/core-api-utils";
import namedSettings from "../../../system/named-settings";

const genericAvatarDataUrl = "data:image/jpeg;base64,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";

global.genericAvatarDataUrl = genericAvatarDataUrl;

const bindings = {
    entityRef: "<",
    iconPlacement: "@",
    tooltipPlacement: "@"
};


const initialState = {
    iconPlacement: "left", // can be left, right, none
    tooltipPlacement: "top", // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
    trigger: "none",
    ready: false,
    genericAvatarDataUrl
};


const entityLoaders = {
    // custom loaders, add more entity types here with links to their CORE_API loader method and
    // a post-processing step (mkProps) to generate a list of { name, value } pairs

    "APPLICATION": {
        method: CORE_API.ApplicationStore.getById,
        mkProps: (app, displayNameService) => ([
            {
                name: "Asset Code",
                value: app.assetCode || "?"
            }, {
                name: "Kind",
                value: displayNameService.lookup("applicationKind", app.applicationKind, "?")
            }, {
                name: "Lifecycle",
                value: displayNameService.lookup("lifecyclePhase", app.lifecyclePhase, "?")
            }, {
                name: "Criticality",
                value: displayNameService.lookup("criticality", app.businessCriticality, "?")
            }
        ])
    },
    "PERSON": {
        method: CORE_API.PersonStore.getById,
        mkProps: (person, displayNameService, serviceBroker) => {
            const orgUnitName = {
                name: "Org Unit",
                value: "-"
            };

            serviceBroker
                .loadViewData(CORE_API.OrgUnitStore.getById, [ person.organisationalUnitId ])
                .then(r => orgUnitName.value = r.data.name);

            return [
                {
                    name: "Title",
                    value: person.title
                }, {
                    name: "Office Phone",
                    value: person.officePhone
                },
                orgUnitName];
        }
    },
    "CHANGE_INITIATIVE": {
        method: CORE_API.ChangeInitiativeStore.getById,
        mkProps: (ci, displayNameService) => ([
            {
                name: "External Id",
                value: ci.externalId|| "?"
            }, {
                name: "Kind",
                value: displayNameService.lookup("changeInitiative", ci.changeInitiativeKind, "?")
            }, {
                name: "Lifecycle",
                value: displayNameService.lookup("changeInitiativeLifecyclePhase", ci.lifecyclePhase, "?")
            }, {
                name: "Start",
                value: ci.startDate
            }, {
                name: "End",
                value: ci.endDate
            }
        ])
    },
    "DATA_TYPE": {
        method: CORE_API.DataTypeStore.getDataTypeById,
        mkProps: (dt) => ([
            {
                name: "Code",
                value: dt.code || "n/a"
            }
    ])
    }
};


function controller(displayNameService, serviceBroker, settingsService) {
    const vm = initialiseData(this, initialState);
    let avatarUrlTemplate = () => genericAvatarDataUrl;

    vm.$onInit = () => {
        settingsService
            .findOrDefault(namedSettings.avatarTemplateUrl, null)
            .then(templateString => {
                if (templateString) {
                    avatarUrlTemplate = _.template(templateString);
                }
            });
    };

    vm.$onChanges = c => {
        if (! vm.entityRef) return;
        if (_.has(entityLoaders, vm.entityRef.kind)) {
            vm.popoverTemplate = "weil-popover-custom";
            vm.trigger = "mouseenter";
        } else {
            vm.popoverTemplate = "weil-popover-basic";
            vm.trigger = vm.entityRef.description || vm.entityRef.lifecyclePhase
                ? "mouseenter"
                : "none";
        }
    };

    vm.lazyLoad = () => {
        const loader = entityLoaders[vm.entityRef.kind];
        if (loader) {
            serviceBroker
                .loadViewData(loader.method, [ vm.entityRef.id ])
                .then(r => {
                    vm.entity = r.data;
                    if (vm.entity) {
                        vm.props = loader.mkProps(vm.entity, displayNameService, serviceBroker);
                    } else {
                        // fall back to basic popover as no entity found
                        vm.popoverTemplate = "weil-popover-basic";
                    }

                    if (vm.entityRef.kind === "PERSON") {
                        vm.imageUrl = avatarUrlTemplate(vm.entity);
                    } else {
                        vm.imageUrl = null;
                    }

                    vm.ready = true;
                });
        }
    };
}


controller.$inject = [
    "DisplayNameService",
    "ServiceBroker",
    "SettingsService"
];


const component = {
    bindings,
    template,
    controller
};


export default component;
