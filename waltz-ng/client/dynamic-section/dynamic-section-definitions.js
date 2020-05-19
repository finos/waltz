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

const appsSection = {
    componentId: "apps-section",
    name: "Applications",
    icon: "desktop",
    id: 1
};

const assetCostsSection = {
    componentId: "asset-costs-section",
    name: "Costs",
    icon: "money",
    id: 2,
};

const appCostsSection = {
    componentId: "app-costs-section",
    name: "Costs",
    icon: "money",
    id: 3,
};

const authSourcesSection = {
    componentId: "auth-sources-section",
    name: "Authoritative Sources",
    icon: "shield",
    id: 4,
};

const bookmarksSection = {
    componentId: "bookmarks-section",
    name: "Bookmarks",
    icon: "rocket",
    id: 5,
};

const changeInitiativeSection = {
    componentId: "change-initiative-section",
    name: "Change Initiatives",
    icon: "paper-plane-o",
    id: 6,
};

const changeLogSection = {
    componentId: "change-log-section",
    icon: "history",
    name: "Changes",
    id: 7,
};

const complexitySection = {
    componentId: "complexity-section",
    icon: "sort-numeric-asc",
    name: "Complexity",
    id: 8,
};

const dataFlowSection = {
    componentId: "data-flow-section",
    name: "Data Flows",
    icon: "random",
    id: 9,
};

const entityNamedNotesSection = {
    componentId: "entity-named-notes-section",
    name: "Notes",
    icon: "sticky-note-o",
    id: 10,
};

const entityStatisticSection = {
    componentId: "entity-statistic-section",
    name: "Indicators",
    icon: "pie-chart",
    id: 11,
};

const entityDiagramsSection = {
    componentId: "entity-diagrams-section",
    name: "Diagrams",
    icon: "picture-o",
    id: 12
};

const involvedPeopleSection = {
    componentId: "involved-people-section",
    name: "People",
    icon: "users",
    id: 13,
};

const logicalFlowsTabgroupSection = {
    componentId: "logical-flows-tabgroup-section",
    name: "Logical Flows",
    icon: "random",
    id: 14
};

const measurableRatingAppSection = {
    componentId: "measurable-rating-app-section",
    name: "Ratings",
    icon: "puzzle-piece",
    id: 15,
};

const measurableRatingsBrowserSection = {
    componentId: "measurable-ratings-browser-section",
    name: "Ratings",
    icon: "star-half-o",
    id: 16,
};

const surveySection = {
    componentId: "survey-section",
    name: "Surveys",
    icon: "wpforms",
    id: 17,
};

const technologySection = {
    componentId: "technology-section",
    name: "Technology",
    icon: "server",
    id: 18,
};

const technologySummarySection = {
    componentId: "technology-summary-section",
    name: "Technologies",
    icon: "server",
    id: 19,
};

const entityStatisticSummarySection = {
    componentId: "entity-statistic-summary-section",
    name: "Indicators",
    icon: "pie-chart",
    id: 20
};

const measurableRatingExplorerSection = {
    componentId: "measurable-rating-explorer-section",
    name: "Ratings",
    icon: "star-half-o",
    id: 21
};

const relatedMeasurablesSection = {
    componentId: "related-measurables-section",
    name: "Related Viewpoints",
    icon: "link",
    id: 22
};

const relatedDataTypesSection = {
    componentId: "related-data-type-section",
    name: "Related Types",
    icon: "qrcode",
    id: 23
};

const relatedAppsSection = {
    componentId: "related-apps-section",
    name: "Related Apps",
    icon: "desktop",
    id: 24
};

const relatedAppGroupsSection = {
    componentId: "related-app-groups-section",
    name: "Related Groups",
    icon: "object-group",
    id: 25
};

const personHierarchySection = {
    componentId: "person-hierarchy-section",
    name: "Hierarchy",
    icon: "address-book-o",
    id: 26
};

const personAppsSection = {
    componentId: "person-apps-section",
    name: "Applications",
    icon: "desktop",
    id: 27
};

const dataTypeOriginatorsSection = {
    componentId: "data-type-originators",
    name: "Originators",
    icon: "inbox",
    id: 28
};

const dataTypeFlowSection = {
    componentId: "data-type-flow-section",
    name: "Data Flows",
    icon: "random",
    id: 29,
};

const relatedPhysicalFieldSection = {
    componentId: "related-physical-spec-defn-field-section",
    name: "Physical Fields",
    icon: "file-code-o",
    id: 40
};

const relatedOrgUnitSection = {
    componentId: "related-org-unit-section",
    name: "Organisation Units",
    icon: "sitemap",
    id: 50
};

const logicalDataElementsSection = {
    componentId: "logical-data-elements-section",
    name: "Logical Data Elements",
    icon: "asterisk",
    id: 60
};

const physicalFlowParticipantsSection = {
    componentId: "physical-flow-participants-section",
    name: "Flow Participants",
    icon: "cubes",
    id: 70
};

const changeSetSection = {
    componentId: "change-set-section",
    name: "Change Sets",
    icon: "hourglass-2",
    id: 80
};

const personChangeSetSection = {
    componentId: "person-change-set-section",
    name: "Change Sets",
    icon: "hourglass-2",
    id: 90
};

const licenceSection = {
    componentId: "licence-section",
    name: "Licences",
    icon: "id-card-o",
    id: 100
};

const flowSpecDefinitionSection = {
    componentId: "flow-spec-definition-section",
    name: "Specification Definition",
    icon: "file-code-o",
    id: 110
};

const attestationSummarySection = {
    componentId: "attestation-summary-section",
    name: "Attestations",
    icon: "check-square-o",
    id: 120
};

const softwarePackageVersions = {
    componentId: "software-package-versions",
    name: "Versions",
    icon: "clock-o",
    id: 130
};

const softwarePackagesSection = {
    componentId: "software-packages-section",
    name: "Software Packages",
    icon: "gift",
    id: 140
};

const orgUnitDirectMeasurableSection = {
    componentId: "org-unit-direct-measurable-section",
    name: "Direct Viewpoints",
    icon: "puzzle-piece",
    id: 150
};

const personMeasurableInvolvementsSection = {
    componentId: "person-measurable-involvements-section",
    name: "Viewpoint Involvements",
    icon: "user-o",
    id: 160
};

export const dynamicSections = {
    appCostsSection,
    appsSection,
    assetCostsSection,
    attestationSummarySection,
    authSourcesSection,
    bookmarksSection,
    changeInitiativeSection,
    changeLogSection,
    changeSetSection,
    complexitySection,
    dataFlowSection,
    dataTypeFlowSection,
    dataTypeOriginatorsSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    entityStatisticSection,
    entityStatisticSummarySection,
    flowSpecDefinitionSection,
    involvedPeopleSection,
    licenceSection,
    logicalDataElementsSection,
    logicalFlowsTabgroupSection,
    measurableRatingAppSection,
    measurableRatingExplorerSection,
    measurableRatingsBrowserSection,
    orgUnitDirectMeasurableSection,
    personAppsSection,
    personChangeSetSection,
    personMeasurableInvolvementsSection,
    personHierarchySection,
    physicalFlowParticipantsSection,
    relatedAppGroupsSection,
    relatedAppsSection,
    relatedDataTypesSection,
    relatedMeasurablesSection,
    relatedOrgUnitSection,
    relatedPhysicalFieldSection,
    softwarePackagesSection,
    softwarePackageVersions,
    surveySection,
    technologySection,
    technologySummarySection
};


function pack(section, children = []) {
    return children
        ? Object.assign({}, section, { children })
        : section;
}


export const dynamicSectionsByKind = {
    "APPLICATION": [
        measurableRatingAppSection,
        entityNamedNotesSection,
        bookmarksSection,
        changeInitiativeSection,
        changeSetSection,
        involvedPeopleSection,
        entityDiagramsSection,
        dataFlowSection,
        technologySection,
        appCostsSection,
        entityStatisticSection,
        surveySection,
        changeLogSection
    ],
    "ACTOR": [
        measurableRatingAppSection,
        bookmarksSection,
        entityNamedNotesSection,
        involvedPeopleSection,
        changeInitiativeSection,
        entityDiagramsSection,
        dataFlowSection,
        appsSection,
        technologySummarySection,
        changeLogSection
    ],
    "CHANGE_INITIATIVE": [
        bookmarksSection,
        changeSetSection,
        entityNamedNotesSection,
        involvedPeopleSection,
        relatedMeasurablesSection,
        pack(relatedAppsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        relatedAppGroupsSection,
        relatedDataTypesSection,
        surveySection,
        entityDiagramsSection,
        logicalFlowsTabgroupSection,
        changeLogSection
    ],
    "ORG_UNIT": [
        pack(appsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        measurableRatingsBrowserSection,
        pack(logicalFlowsTabgroupSection,
            [authSourcesSection]),
        changeInitiativeSection,
        changeSetSection,
        involvedPeopleSection,
        attestationSummarySection,
        bookmarksSection,
        changeLogSection,
        orgUnitDirectMeasurableSection
    ],
    "MEASURABLE": [
        pack(appsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        entityNamedNotesSection,
        measurableRatingExplorerSection,
        relatedMeasurablesSection,
        bookmarksSection,
        changeSetSection,
        involvedPeopleSection,
        pack(logicalFlowsTabgroupSection,
            [authSourcesSection]),
        entityDiagramsSection,
        changeLogSection
    ],
    "PERSON": [
        personHierarchySection,
        pack(personAppsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        attestationSummarySection,
        bookmarksSection,
        changeInitiativeSection,
        personChangeSetSection,
        pack(logicalFlowsTabgroupSection,
            [authSourcesSection]),
        surveySection,
        changeLogSection,
        measurableRatingsBrowserSection,
        personMeasurableInvolvementsSection
    ],
    "DATA_TYPE": [
        appsSection,
        entityNamedNotesSection,
        bookmarksSection,
        involvedPeopleSection,
        authSourcesSection,
        dataTypeOriginatorsSection,
        logicalFlowsTabgroupSection,
        logicalDataElementsSection,
        changeLogSection
    ],
    "APP_GROUP": [
        pack(appsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        pack(logicalFlowsTabgroupSection,
            [authSourcesSection]),
        attestationSummarySection,
        changeInitiativeSection,
        changeSetSection,
        measurableRatingsBrowserSection,
        relatedAppGroupsSection,
        bookmarksSection,
        entityNamedNotesSection,
        relatedDataTypesSection,
        relatedMeasurablesSection,
        involvedPeopleSection,
        changeLogSection
    ],
    "SCENARIO": [
        changeInitiativeSection,
        logicalFlowsTabgroupSection,
        pack(appsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        bookmarksSection,
        entityNamedNotesSection,
        changeLogSection
    ],
    "FLOW_DIAGRAM": [
        pack(appsSection,
            [assetCostsSection, complexitySection, entityStatisticSummarySection, technologySummarySection]),
        bookmarksSection,
        changeLogSection,
        measurableRatingsBrowserSection
    ],
    "PHYSICAL_FLOW": [
        bookmarksSection,
        changeLogSection,
        entityDiagramsSection,
        entityNamedNotesSection,
        flowSpecDefinitionSection,
        involvedPeopleSection,
        physicalFlowParticipantsSection
    ]
};
