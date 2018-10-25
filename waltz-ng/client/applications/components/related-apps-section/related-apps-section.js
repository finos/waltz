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

import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {mapToDisplayNames} from "../../application-utils";
import {relationshipKind} from "../../../common/services/enums/relationship-kind";
import {CORE_API} from "../../../common/services/core-api-utils";
import {getDefaultScopeForEntityKind, mkSelectionOptions} from "../../../common/selector-utils";
import {sameRef} from "../../../common/entity-utils";
import {
    allowedRelationshipsByKind,
    changeRelationshipFunctionsByKind,
    fetchRelationshipFunctionsByKind
} from "../../../common/relationship-utils";


import template from "./related-apps-section.html";


const bindings = {
    editRole: "@?",
    parentEntityRef: "<",
};


const initialState = {
    columnDefs: [
        Object.assign(mkEntityLinkGridCell("Name", "app"), { width: "25%"} ),
        { field: "relationshipDisplay", name: "Relationship"},
        { field: "app.assetCode"},
        { field: "app.kindDisplay", name: "Kind"},
        { field: "app.overallRatingDisplay", name: "Overall Rating"},
        { field: "app.businessCriticalityDisplay", name: "Business Criticality"},
        { field: "app.lifecyclePhaseDisplay", name: "Lifecycle Phase"},
    ],
    allowedRelationships: [],
    entityRelationships: [],
    visibility: {
        editor: false
    }
};


function mkGridData(relations = [], apps = []) {
    const appsById = _.keyBy(apps, "id");

    return _.map(relations, r => ({
        relationshipDisplay: relationshipKind[r.relationship] ? relationshipKind[r.relationship].name : r.relationship,
        app: appsById[r.entity.id]
    }));
}


function mkChangeCommand(operation, entityRef, relKind) {
    return {
        operation,
        entityReference: {
            id: entityRef.id,
            kind: "APPLICATION"
        },
        relationship: relKind
    };
}


function controller($q,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadData(force = false) {
        const fetchRelationships = fetchRelationshipFunctionsByKind[vm.parentEntityRef.kind];

        const relationsPromise = serviceBroker
            .loadViewData(
                fetchRelationships,
                [ vm.parentEntityRef.id ],
                { force })
            .then(r => _
                .chain(r.data)
                .flatMap(rel => ([
                    {entity: rel.a, relationship: rel.relationship},
                    {entity: rel.b, relationship: rel.relationship}
                ]))
                .filter(rel => rel.entity.kind === "APPLICATION")
                .reject(rel => sameRef(rel.entity, vm.parentEntityRef, { skipChecks: true }))
                .value());

        const appsPromise = serviceBroker.loadViewData(
            CORE_API.ApplicationStore.findBySelector,
            [ mkSelectionOptions(
                vm.parentEntityRef,
                getDefaultScopeForEntityKind(vm.parentEntityRef.kind),
                ["ACTIVE", "PENDING", "REMOVED"])
            ],
            { force })
            .then(r => _.map(r.data, a => Object.assign({}, a, mapToDisplayNames(a))));

        return $q.all([appsPromise, relationsPromise])
            .then(([apps, relations]) => {
                vm.gridData = mkGridData(relations, apps);
                vm.entityRelationships = _.map(relations, r => Object.assign({}, {
                    relationship: r.relationship,
                    entity: {
                        id: r.entity.id,
                        name: r.entity.name,
                        kind: "APPLICATION"
                    }
                }));
            });
    }

    vm.$onChanges= (c) => {
        if (vm.parentEntityRef) {
            vm.allowedRelationships = allowedRelationshipsByKind[vm.parentEntityRef.kind];
            loadData(false);
        }
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn("app-relationships.csv");
    };

    vm.editMode = (editMode) => {
        vm.visibility.editor = editMode;
    };

    vm.onAdd = (entityRel) => {
        const changeRelationship = changeRelationshipFunctionsByKind[vm.parentEntityRef.kind];
        return serviceBroker
            .execute(
                changeRelationship,
                [vm.parentEntityRef.id, mkChangeCommand("ADD", entityRel.entity, entityRel.relationship)])
            .then(result => {
                if(result.data) {
                    notification.success("Relationship added successfully");
                } else {
                    notification.warning("Failed to add relationship")
                }
                return loadData(true);
            });
    };

    vm.onRemove = (entityRel) => {
        const changeRelationship = changeRelationshipFunctionsByKind[vm.parentEntityRef.kind];
        return serviceBroker
            .execute(
                changeRelationship,
                [vm.parentEntityRef.id, mkChangeCommand("REMOVE", entityRel.entity, entityRel.relationship)])
            .then(result => {
                if(result.data) {
                    notification.success("Relationship removed successfully");
                } else {
                    notification.warning("Failed to remove relationship")
                }
                return loadData(true);
            });
    };
}


controller.$inject = [
    "$q",
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};

const id = "waltzRelatedAppsSection";


export default {
    id,
    component
};