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

import _ from "lodash";
import { initialiseData } from "../../../common";
import template from "./entity-icon-label.html";
import { CORE_API } from "../../services/core-api-utils";
import namedSettings from "../../../system/named-settings";
import { mkSelectionOptions } from "../../selector-utils";
import { mkRef } from "../../entity-utils";
import { genericAvatarDataUrl } from "../../../person/person-utils";

const bindings = {
    entityRef: "<",
    iconPlacement: "@?",
    additionalDisplayData: "<?",
    showExternalId: "<?",
    tooltipPlacement: "@?",
    popoverDelay: "<?"
};


const initialState = {
    iconPlacement: "left", // can be left, right, none
    tooltipPlacement: "top", // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
    showExternalId: false,
    trigger: "none",
    ready: false,
    additionalDisplayData: [],
    genericAvatarDataUrl,
    popoverDelay: 300
};


const entityLoaders = {
    // custom loaders, add more entity types here with links to their CORE_API loader method and
    // a post-processing step (mkProps) to generate a list of { name, value } pairs

    "ORG_UNIT": {
        method: CORE_API.OrgUnitStore.getById,
        mkProps: (ou, displayNameService) => ([
            {
                name: "External Id",
                value: ou.externalId || "-"
            }
        ])
    },
    "MEASURABLE": {
        method: CORE_API.MeasurableStore.getById,
        mkProps: (measurable, displayNameService) => ([
            {
                name: "External Id",
                value: measurable.externalId || "-"
            }
        ])
    },
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
    "FLOW_CLASSIFICATION_RULE": {
        method: CORE_API.FlowClassificationRuleStore.getById,
        mkProps: (rule) => ([
            {
                name: "Type",
                value: "Flow Classification Rule"
            }, {
                name: "Application",
                value: _.get(rule, ["subjectReference", "name"], "?")
            }, {
                name: "Scope",
                value: _.get(rule, ["vantagePointReference", "name"], "?")
            }
        ])
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
    "PERSON": {
        method: CORE_API.PersonStore.getById,
        mkProps: (person, displayNameService, serviceBroker) => {
            const orgUnitName = {
                name: "Org Unit",
                value: "-"
            };

            if (person.organisationalUnitId) {
                serviceBroker
                    .loadViewData(CORE_API.OrgUnitStore.getById, [ person.organisationalUnitId ])
                    .then(r => orgUnitName.value = r.data.name);
            }

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
    "PHYSICAL_FLOW": {
        method: CORE_API.PhysicalFlowStore.getById,
        mkProps: (flow, displayNameService, serviceBroker) => {
            const specificationFormat = {
                name: "Format",
                value: "-"
            };

            serviceBroker
                .loadViewData(CORE_API.PhysicalSpecificationStore.getById, [flow.specificationId])
                .then(r => {
                    specificationFormat.value = displayNameService.lookup("dataFormatKind", r.data.format, "?");
                    flow.name = r.data.name;
                    flow.description = flow.description || r.data.description;
                });

            return [
                {
                    name: "External Id",
                    value: flow.externalId
                }, {
                    name: "Criticality",
                    value: displayNameService.lookup("criticality", flow.criticality, "?")
                }, {
                    name: "Transport",
                    value: displayNameService.lookup("TransportKind", flow.transport, "?")
                }, {
                    name: "Frequency",
                    value: displayNameService.lookup("frequencyKind", flow.frequency, "?")
                },
                specificationFormat,
                {
                    name: "Provenance",
                    value: flow.provenance
                }];
        }
    },
    "PHYSICAL_SPECIFICATION": {
        method: CORE_API.PhysicalSpecificationStore.getById,
        mkProps: (spec, displayNameService, serviceBroker) => {
            return [
                {
                    name: "Owning Entity",
                    value: spec.owningEntity.name
                }, {
                    name: "Format",
                    value: displayNameService.lookup("dataFormatKind", spec.format, "?")
                }, {
                    name: "Provenance",
                    value: spec.provenance
                }];
        }
    },
    "DATA_TYPE": {
        method: CORE_API.DataTypeStore.getDataTypeById,
        mkProps: (dt) => ([
            {
                name: "Code",
                value: dt.code || "n/a"
            }
        ])
    },
    "SOFTWARE_VERSION": {
        method: CORE_API.SoftwareCatalogStore.getByVersionId,
        mkProps: (catalog, displayNameService, serviceBroker) => {
            const version = _.get(catalog, ["versions", 0], null);
            const softwarePackage = _.get(catalog, ["packages", 0], null);
            const usages = _.get(catalog, "usages", []);

            const appUsages = _.chain(usages)
                .map(u => u.applicationId)
                .uniq()
                .value();

            const licenceProperty = {
                name: "Licence(s)",
                value: "-"
            };

            const options = mkSelectionOptions(mkRef("SOFTWARE_VERSION", version.id));

            serviceBroker
                .loadViewData(CORE_API.LicenceStore.findBySelector, [options])
                .then(r => {
                    licenceProperty.value = _.chain(r.data)
                        .map(r => r.externalId)
                        .join(", ")
                        .value();
                });

            return [
                {
                    name: "Name",
                    value: _.get(softwarePackage, "name", "-")
                },
                {
                    name: "Version",
                    value: _.get(version, "version", "-")
                },
                {
                    name: "External Id",
                    value: _.get(version, "externalId", "-")
                },
                {
                    name: "Release Date",
                    value: _.get(version, "releaseDate", "-")
                },
                {
                    name: "Description",
                    value: _.get(version, "description", "-")
                },
                licenceProperty,
                {
                    name: "Usage",
                    value: appUsages.length + " applications"
                },
            ];
        }
    },
    "SOFTWARE": {
        method: CORE_API.SoftwareCatalogStore.getByPackageId,
        mkProps: (catalog, displayNameService, serviceBroker) => {
            const versions = _.get(catalog, ["versions"], null);
            const softwarePackage = _.get(catalog, ["packages", 0], null);
            const usages = _.get(catalog, "usages", []);

            const appUsages = _.chain(usages)
                .map(u => u.applicationId)
                .uniq()
                .value();

            const licenceProperty = {
                name: "Licence(s)",
                value: "-"
            };

            const options = mkSelectionOptions(mkRef("SOFTWARE", softwarePackage.id));

            serviceBroker
                .loadViewData(CORE_API.LicenceStore.findBySelector, [options])
                .then(r => {
                    licenceProperty.value = _.chain(r.data)
                        .map(r => r.externalId)
                        .join(", ")
                        .value();
                });

            return [
                {
                    name: "Name",
                    value: _.get(softwarePackage, "name", "-")
                },
                {
                    name: "Vendor",
                    value: _.get(softwarePackage, "releaseDate", "-")
                },
                {
                    name: "External Id",
                    value: _.get(softwarePackage, "externalId", "-")
                },
                {
                    name: "Description",
                    value: _.get(softwarePackage, "description", "-")
                },
                {
                    name: "Versions",
                    value: _.chain(versions)
                        .map(r => r.name)
                        .join(", ")
                        .value()
                },
                licenceProperty,
                {
                    name: "Usage",
                    value: appUsages.length + " applications"
                },
            ];
        }
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
            vm.trigger = "mouseenter click";
        } else {
            vm.popoverTemplate = "weil-popover-basic";
            vm.trigger = vm.entityRef.description || vm.entityRef.lifecyclePhase
                ? "mouseenter click"
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
