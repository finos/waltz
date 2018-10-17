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

export const dynamicSections = {
    appsSection: {
        componentId: "apps-section",
        name: "Applications",
        icon: "desktop",
        id: 1,
    },
    assetCostsSection: {
        componentId: "asset-costs-section",
        name: "Costs",
        icon: "money",
        id: 2,
    },
    appCostsSection: {
        componentId: "app-costs-section",
        name: "Costs",
        icon: "money",
        id: 3,
    },
    authSourcesSection: {
        componentId: "auth-sources-section",
        name: "Authoritative Sources",
        icon: "shield",
        id: 4,
    },
    bookmarksSection: {
        componentId: "bookmarks-section",
        name: "Bookmarks",
        icon: "rocket",
        id: 5,
    },
    changeInitiativeSection: {
        componentId: "change-initiative-section",
        name: "Change Initiatives",
        icon: "paper-plane-o",
        id: 6,
    },
    changeLogSection: {
        componentId: "change-log-section",
        icon: "history",
        name: "Changes",
        id: 7,
    },
    complexitySection: {
        componentId: "complexity-section",
        icon: "sort-numeric-asc",
        name: "Complexity",
        id: 8,
    },
    dataFlowSection: {
        componentId: "data-flow-section",
        name: "Data Flows",
        icon: "random",
        id: 9,
    },
    entityNamedNotesSection: {
        componentId: "entity-named-notes-section",
        name: "Notes",
        icon: "sticky-note-o",
        id: 10,
    },
    entityStatisticSection: {
        componentId: "entity-statistic-section",
        name: "Indicators",
        icon: "pie-chart",
        id: 11,
    },
    involvedPeopleSection: {
        componentId: "involved-people-section",
        name: "People",
        icon: "users",
        id: 13,
    },
    logicalFlowsTabgroupSection: {
        componentId: "logical-flows-tabgroup-section",
        name: "Logical Flows",
        icon: "random",
        id: 14,
    },
    measurableRatingAppSection: {
        componentId: "measurable-rating-app-section",
        name: "Ratings",
        icon: "puzzle-piece",
        id: 15,
    },
    measurableRatingsBrowserSection: {
        componentId: "measurable-ratings-browser-section",
        name: "Ratings",
        icon: "star-half-o",
        id: 16,
    },
    surveySection: {
        componentId: "survey-section",
        name: "Surveys",
        icon: "wpforms",
        id: 17,
    },
    technologySection: {
        componentId: "technology-section",
        name: "Technology",
        icon: "server",
        id: 18,
    },
    technologySummarySection: {
        componentId: "technology-summary-section",
        name: "Technologies",
        icon: "server",
        id: 19,
    },
    entityStatisticSummarySection: {
        componentId: "entity-statistic-summary-section",
        name: "Indicators",
        icon: "pie-chart",
        id: 20
    },
    measurableRatingExplorerSection: {
        componentId: "measurable-rating-explorer-section",
        name: "Ratings",
        icon: "star-half-o",
        id: 21
    },
    relatedMeasurablesSection: {
        componentId: "related-measurables-section",
        name: "Related Viewpoints",
        icon: "link",
        id: 22
    },
    relatedDataTypesSection: {
        componentId: "related-data-type-section",
        name: "Related Types",
        icon: "qrcode",
        id: 23
    },
    relatedAppsSection: {
        componentId: "related-apps-section",
        name: "Related Apps",
        icon: "desktop",
        id: 24
    },
    relatedAppGroupsSection: {
        componentId: "related-app-groups-section",
        name: "Related Groups",
        icon: "object-group",
        id: 25
    },
    personHierarchySection: {
        componentId: "person-hierarchy-section",
        name: "Hierarchy",
        icon: "address-book-o",
        id: 26
    },
    personAppsSection: {
        componentId: "person-apps-section",
        name: "Applications",
        icon: "desktop",
        id: 27
    },
    dataTypeOriginatorsSection: {
        componentId: "data-type-originators",
        name: "Originators",
        icon: "inbox",
        id: 28
    },
    dataTypeFlowSection: {
        componentId: "data-type-flow-section",
        name: "Data Flows",
        icon: "random",
        id: 29
    },
    changeInitiativeNavigatorSection: {
        componentId: "change-initiative-navigator-section",
        name: "Change Initiatives",
        icon: "paper-plane-o",
        id: 30,
    },
    relatedPhysicalFieldSection: {
        componentId: "related-physical-spec-defn-field-section",
        name: "Physical Fields",
        icon: "file-code-o",
        id: 40,
    },
    relatedOrgUnitSection: {
        componentId: "related-org-unit-section",
        name: "Organisation Units",
        icon: "sitemap",
        id: 50,
    },
    entityDiagramsSection: {
        componentId: "entity-diagrams-section",
        name: "Diagrams",
        icon: "picture-o",
        id: 12
    }

};



export const dynamicSectionsByKind = {
    "APPLICATION": [
        dynamicSections.measurableRatingAppSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.bookmarksSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.changeInitiativeSection,
        dynamicSections.entityDiagramsSection,
        dynamicSections.dataFlowSection,
        dynamicSections.technologySection,
        dynamicSections.appCostsSection,
        dynamicSections.entityStatisticSection,
        dynamicSections.surveySection,
        dynamicSections.changeLogSection
    ],
    "ACTOR": [
        dynamicSections.measurableRatingAppSection,
        dynamicSections.bookmarksSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.changeInitiativeSection,
        dynamicSections.entityDiagramsSection,
        dynamicSections.dataFlowSection,
        dynamicSections.appsSection,
        dynamicSections.technologySummarySection,
        dynamicSections.changeLogSection
    ],
    "CHANGE_INITIATIVE": [
        dynamicSections.bookmarksSection,
        dynamicSections.changeInitiativeNavigatorSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.relatedMeasurablesSection,
        dynamicSections.relatedAppsSection,
        dynamicSections.relatedAppGroupsSection,
        dynamicSections.relatedDataTypesSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.technologySummarySection,
        dynamicSections.surveySection,
        dynamicSections.entityDiagramsSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.changeLogSection
    ],
    "ORG_UNIT": [
        dynamicSections.measurableRatingsBrowserSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.authSourcesSection,
        dynamicSections.changeInitiativeSection,
        dynamicSections.complexitySection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.appsSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.technologySummarySection,
        dynamicSections.assetCostsSection,
        dynamicSections.bookmarksSection,
        dynamicSections.changeLogSection
    ],
    "MEASURABLE": [
        dynamicSections.entityNamedNotesSection,
        dynamicSections.measurableRatingExplorerSection,
        dynamicSections.relatedMeasurablesSection,
        dynamicSections.bookmarksSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.appsSection,
        dynamicSections.authSourcesSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.complexitySection,
        dynamicSections.technologySummarySection,
        dynamicSections.assetCostsSection,
        dynamicSections.entityDiagramsSection,
        dynamicSections.changeLogSection
    ],
    "PERSON": [
        dynamicSections.personHierarchySection,
        dynamicSections.personAppsSection,
        dynamicSections.bookmarksSection,
        dynamicSections.changeInitiativeSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.authSourcesSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.assetCostsSection,
        dynamicSections.complexitySection,
        dynamicSections.technologySummarySection,
        dynamicSections.changeLogSection
    ],
    "DATA_TYPE": [
        dynamicSections.entityNamedNotesSection,
        dynamicSections.bookmarksSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.authSourcesSection,
        dynamicSections.dataTypeOriginatorsSection,
        dynamicSections.dataTypeFlowSection,
        dynamicSections.changeLogSection
    ],
    "APP_GROUP": [
        dynamicSections.changeInitiativeSection,
        dynamicSections.measurableRatingsBrowserSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.authSourcesSection,
        dynamicSections.appsSection,
        dynamicSections.relatedAppGroupsSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.complexitySection,
        dynamicSections.technologySummarySection,
        dynamicSections.assetCostsSection,
        dynamicSections.bookmarksSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.relatedDataTypesSection,
        dynamicSections.relatedMeasurablesSection,
        dynamicSections.changeLogSection
    ],
    "SCENARIO": [
        dynamicSections.changeInitiativeSection,
        dynamicSections.logicalFlowsTabgroupSection,
        dynamicSections.appsSection,
        dynamicSections.entityStatisticSummarySection,
        dynamicSections.complexitySection,
        dynamicSections.technologySummarySection,
        dynamicSections.assetCostsSection,
        dynamicSections.bookmarksSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.changeLogSection
    ]
};