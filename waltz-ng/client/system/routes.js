

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


import SystemAdminList from "./system-admin-list";
import SettingsView from "./settings-view";
import HierarchiesView from "./hierarchies-view";
import OrphansView from "./orphans-view";
import RecalculateView from "./recalculate-view";
import ActorsView from "./actors-view";
import EntityNamedNoteTypesView from "./entity-named-node-types-view";
import StaticPanelsView from "./static-panels-view";
import StaticDiagramsView from "./static-diagrams-view";
import StaticDiagramView from "./static-diagram-view";
import AssessmentDefinitionsView from "./assessment-defintions-view";
import RatingSchemesView from "./rating-schemes-view";
import EudaListView from "./euda-list-view";
import RelationshipKindsView from "./relationship-kinds-view";
import ReassignRecipientsView from "./reassign-recipients-view";
import ColorGradientView from "./color-gradient-view";
import PermissionsView from "./permissions-view";
import NavAidBuilderView from "./nav-aid-builder-view";
import VersionInfoView from "./version-info-view";
import LicencesAdminView from "./licences-view";
import MeasurableCategoryListView from "./measurable-category-list-view";
import MeasurableCategoryEditView from "./measurable-category-edit-view";
import MeasurableCategoryCreateView from "./measurable-category-create-view";
import DiagramBuilderView from "./diagram-builder-view";
import NavAidAdminView from "./nav-aids-view";


const baseState = {
    url: "system"
};


const listViewState = {
    url: "/list",
    views: {"content@": SystemAdminList}
};


const settingsState = {
    url: "/settings",
    views: { "content@": SettingsView }
};


const hierarchiesState = {
    url: "/hierarchies",
    views: { "content@": HierarchiesView }
};


const orphansState = {
    url: "/orphans",
    views: { "content@": OrphansView }
};


const recalculateState = {
    url: "/recalculate",
    views: { "content@": RecalculateView }
};


const reassignRecipientsState = {
    url: "/reassign-recipients",
    views: { "content@": ReassignRecipientsView }
};


const actorsState = {
    url: "/actors",
    views: { "content@": ActorsView }
};


const entityNamedNodeTypesState = {
    url: "/entity-named-note-types",
    views: {"content@": EntityNamedNoteTypesView}
};


const staticPanelsState = {
    url: "/static-panels",
    views: {"content@": StaticPanelsView}
};

const staticDiagramsState = {
    url: "/static-diagrams",
    views: {"content@": StaticDiagramsView}
}

const staticDiagramState = {
    url: "/static-diagrams/{id:int}",
    views: {"content@": StaticDiagramView}
}

const assessmentDefintionsState = {
    url: "/assessment-definitions",
    views: {"content@": AssessmentDefinitionsView}
};


const ratingSchemesState = {
    url: "/rating-schemes",
    views: { "content@": RatingSchemesView }
};


const licencesState = {
    url: "/licences",
    views: { "content@": LicencesAdminView }
};




const measurableCategoryState = {
    url: "/measurable-category",
};

const measurableCategoryListState = {
    url: "/list",
    views: { "content@": MeasurableCategoryListView }
};


const measurableCategoryCreateState = {
    url: "/create",
    views: { "content@": MeasurableCategoryCreateView }
};


const measurableCategoryEditState = {
    url: "/id/{id:int}/edit",
    views: { "content@": MeasurableCategoryEditView }
};


const eudaListState = {
    url: "/euda-list",
    views: {"content@": EudaListView}
};


const relationshipKindsState = {
    url: "/relationship-kinds",
    views: {"content@": RelationshipKindsView}
};


const colorGradientState = {
    url: "/color-gradient",
    views: {"content@": ColorGradientView}
};


const navAidBuilderState = {
    url: "/nav-aid-builder",
    views: {"content@": NavAidBuilderView}
};

const navAidAdminState = {
    url: "/nav-aids",
    views: {"content@": NavAidAdminView}
};

const versionInfoState = {
    url: "/version-info",
    views: {"content@": VersionInfoView}
};

const diagramBuilderState = {
    url: "/diagram-builder",
    views: {"content@": DiagramBuilderView}
};

const permissionsState = {
    url: "/permissions",
    views: {"content@": PermissionsView}
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state("main.system", baseState)
        .state("main.system.list", listViewState)
        .state("main.system.actors", actorsState)
        .state("main.system.assessment-definitions", assessmentDefintionsState)
        .state("main.system.color-gradient", colorGradientState)
        .state("main.system.diagram-builder", diagramBuilderState)
        .state("main.system.entity-named-note-types", entityNamedNodeTypesState)
        .state("main.system.euda-list", eudaListState)
        .state("main.system.hierarchies", hierarchiesState)
        .state("main.system.licences", licencesState)
        .state("main.system.measurable-category", measurableCategoryState)
        .state("main.system.measurable-category.list", measurableCategoryListState)
        .state("main.system.measurable-category.edit", measurableCategoryEditState)
        .state("main.system.measurable-category.create", measurableCategoryCreateState)
        .state("main.system.nav-aids", navAidAdminState)
        .state("main.system.nav-aid-builder", navAidBuilderState)
        .state("main.system.orphans", orphansState)
        .state("main.system.permissions", permissionsState)
        .state("main.system.rating-schemes", ratingSchemesState)
        .state("main.system.reassign-recipients", reassignRecipientsState)
        .state("main.system.recalculate", recalculateState)
        .state("main.system.relationship-kinds", relationshipKindsState)
        .state("main.system.settings", settingsState)
        .state("main.system.static-diagrams", staticDiagramsState)
        .state("main.system.static-diagram", staticDiagramState)
        .state("main.system.static-panels", staticPanelsState)
        .state("main.system.version-info", versionInfoState);
}


setupRoutes.$inject = ["$stateProvider"];


export default setupRoutes;