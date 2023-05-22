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
import {initialiseData} from "../common";
import _ from "lodash";
import template from "./system-admin-list.html";

/**
 * @name adminOption
 * @type {role: string, name: string, icon: string, description: string, state: string}
 */

/**
 * @name adminSection
 * @type {name: string, options: adminOption[]}
 */


/** @type {adminOption[]} */
const userManagementOptions = [
    {
        name: "Manage Users",
        role: "USER_ADMIN",
        description: "Register users and alter their permissions",
        state: "main.user.management",
        icon: "users"
    }, {
        name: "User Logs",
        role: "USER_ADMIN",
        description: "See information about accessed pages and actions performed by users",
        state: "main.user.log",
        icon: "list-ul"
    }, {
        name: "Active Users",
        role: "USER_ADMIN",
        description: "See which have been recently active",
        state: "main.user.active",
        icon: "line-chart"
    }, {
        name: "Custom Roles",
        role: "USER_ADMIN",
        description: "View and create custom roles to manage user access",
        state: "main.role.list",
        icon: "users"
    }
];


/** @type {adminOption[]} */
const referenceDataOptions= [
    {
        name: "Actors",
        role: "ACTOR_ADMIN",
        description: "View and edit system actors (both internal and external)",
        state: "main.system.actors",
        icon: "diamond"
    }, {
        name: "Assessment Definitions",
        role: "ADMIN",
        description: "View and edit assessment definitions",
        state: "main.system.assessment-definitions",
        icon: "puzzle-piece"
    }, {
        name: "Entity Named Note Types",
        role: "ADMIN",
        description: "View and edit system entity named node types",
        state: "main.system.entity-named-note-types",
        icon: "sticky-note"
    }, {
        name: "EUDA Promotion",
        role: "EUDA_ADMIN",
        description: "View EUDAs and promote to applications",
        state: "main.system.euda-list",
        icon: "table"
    }, {
        name: "Involvement Kinds",
        role: "ADMIN",
        description: "View and edit system involvement kinds",
        state: "main.involvement-kind.list",
        icon: "id-badge"
    }, {
        name: "Measurable Categories",
        role: "ADMIN",
        description: "View and edit measurable categories",
        state: "main.system.measurable-categories",
        icon: "puzzle-piece"
    }, {
        name: "Rating Schemes",
        role: "ADMIN",
        description: "View and edit rating schemes",
        state: "main.system.rating-schemes",
        icon: "puzzle-piece"
    }, {
        name: "Relationship Kinds",
        role: "ADMIN",
        description: "View and edit relationship kinds",
        state: "main.system.relationship-kinds",
        icon: "link"
    }, {
        name: "Static Panels",
        role: "ADMIN",
        description: "View and edit static panels",
        state: "main.system.static-panels",
        icon: "code"
    }
];


/** @type {adminOption[]} */
const internalOptions= [
    {
        name: "Settings",
        role: "ADMIN",
        description: "View and edit configuration settings",
        state: "main.system.settings",
        icon: "cog"
    }, {
        name: "Version Information",
        role: "ADMIN",
        description: "See Version Information about this installation",
        state: "main.system.version-info",
        icon: "bullseye"
    }
];


/** @type {adminOption[]} */
const utilityOptions= [
    {
        name: "Navigation Aid (navaid) Builder (WIP)",
        role: "ADMIN",
        description: "Helps create navigation aids for measurables, people hierarchies, org hierarchies, etc.",
        state: "main.system.nav-aid-builder",
        icon: "picture-o"
    }, {
        name: "Colour Gradient",
        role: "ADMIN",
        description: "Useful for creating a smooth gradient of colours. A common use-case is for smoothly differentiating between rating scheme items",
        state: "main.system.color-gradient",
        icon: "paint-brush"
    }
];


/** @type {adminOption[]} */
const maintenanceOptions= [
    {
        name: "Hierarchy Maintenance",
        role: "ADMIN",
        description: "Rebuilds hierarchies, should be run whenever the data changes",
        state: "main.system.hierarchies",
        icon: "sitemap"
    },  {
        name: "Recalculate Derived Fields",
        role: "ADMIN",
        description: "Recompute: Data Type Usages, Flow Authoritative Source Ratings",
        state: "main.system.recalculate",
        icon: "calculator"
    },  {
        name: "Orphan entities",
        role: "ADMIN",
        description: "View entities that have erroneously become orphaned",
        state: "main.system.orphans",
        icon: "child"
    },  {
        name: "Reassign recipients",
        role: "ADMIN",
        description: "Reassign attestations or surveys to recipients based on involvement kinds",
        state: "main.system.reassign-recipients",
        icon: "user"
    }
];


/** @type {adminSection[]} */
const allSections = [
    { name: "User Management", options: userManagementOptions },
    { name: "Maintenance", options: maintenanceOptions },
    { name: "Reference Data", options: referenceDataOptions },
    { name: "Internals", options: internalOptions },
    { name: "Utilities", options: utilityOptions }
];


const initialState = {
    sections: []
};

function controller(userService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        userService
            .whoami(true) // force
            .then(user => {
                const roles = user.roles;
                vm.sections = _
                    .chain(allSections)
                    .map(s => ({
                        name: s.name,
                        options: _.filter(
                            s.options,
                            opt => _.includes(roles, "ADMIN")
                                || _.includes(roles, opt.role))}))
                    .filter(s => !_.isEmpty(s.options))
                    .value();
            });
    };
}

controller.$inject = [ "UserService" ];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};
