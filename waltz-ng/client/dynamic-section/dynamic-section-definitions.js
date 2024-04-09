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
import BookmarkPanel from "../bookmarks/svelte/BookmarkPanel.svelte";
import InvolvementsSection from "../involvement-kind/components/svelte/InvolvementsSection.svelte";


const appsSection = {
    componentId: "apps-section",
    name: "Applications",
    icon: "desktop",
    description: "Applications related to this entity",
    id: 1
};

const appCostsSection = {
    componentId: "app-costs-section",
    name: "Costs",
    icon: "money",
    description: "Costs related to this application",
    id: 3,
};

const flowClassificationRulesSection = {
    componentId: "flow-classification-rules-section",
    name: "Flow Classification Rules",
    icon: "shield",
    description: "Flow classification rules related to this entity via consuming applications",
    id: 4,
};

const bookmarksSection = {
    svelteComponent: BookmarkPanel,
    componentId: "bookmarks-section",
    name: "Bookmarks",
    icon: "rocket",
    description: "Bookmarks related to this entity",
    id: 5,
};

const changeInitiativeSection = {
    componentId: "change-initiative-section",
    name: "Change Initiatives",
    icon: "paper-plane-o",
    description: "Change initiatives which have been linked to this entity",
    id: 6,
};

const changeLogSection = {
    componentId: "change-log-section",
    icon: "history",
    name: "Changes",
    description: "A log of recent changes made to this entity, or other entities in waltz for which this is the parent",
    id: 7,
};

const dataFlowSection = {
    componentId: "data-flow-section",
    name: "Data Flows",
    icon: "random",
    description: "Logical and physical data flows related to this entity",
    id: 9,
};

const entityNamedNotesSection = {
    componentId: "entity-named-notes-section",
    name: "Notes",
    icon: "sticky-note-o",
    description: "Notes related to this entity",
    id: 10,
};

const entityStatisticSection = {
    componentId: "entity-statistic-section",
    name: "Statistics",
    icon: "pie-chart",
    description: "Statistics for this entity",
    id: 11,
};

const entityDiagramsSection = {
    componentId: "entity-diagrams-section",
    name: "Diagrams",
    icon: "picture-o",
    description: "Diagrams to which this entity has been associated, either by appearing in the diagram or as a related entity",
    id: 12
};

const involvedPeopleSection = {
    componentId: "involved-people-section",
    name: "People",
    icon: "users",
    description: "People with an involvement to this entity",
    id: 13,
};

const logicalFlowsTabgroupSection = {
    componentId: "logical-flows-tabgroup-section",
    name: "Data Flows",
    icon: "random",
    description: "Logical flow summary based upon applications",
    id: 14
};

const measurableRatingAppSection = {
    componentId: "measurable-rating-app-section",
    name: "Ratings / Roadmaps",
    icon: "puzzle-piece",
    description: "Viewpoints linked to this application with a rating",
    id: 15,
};

const measurableRatingsBrowserSection = {
    componentId: "measurable-ratings-browser-section",
    name: "Ratings / Roadmaps",
    icon: "star-half-o",
    description: "Summary of ratings, allocations and roadmaps related to this entity via applications",
    id: 16,
};

const surveySection = {
    componentId: "survey-section",
    name: "Surveys",
    icon: "wpforms",
    description: "Surveys issued against this entity or to this person",
    id: 17,
};

const technologySection = {
    componentId: "technology-section",
    name: "Technology",
    icon: "server",
    description: "Databases, servers, licences, software and custom environments linked to this application",
    id: 18,
};

const technologySummarySection = {
    componentId: "technology-summary-section",
    name: "Technologies",
    icon: "server",
    description: "Summary charts of databases, servers, licences and software used by applications related to this entity",
    id: 19,
};

const entityStatisticSummarySection = {
    componentId: "entity-statistic-summary-section",
    name: "Statistics",
    icon: "pie-chart",
    description: "Summarised statistics for apps related to this entity, can be used to drill down into history",
    id: 20
};

const measurableRatingExplorerSection = {
    componentId: "measurable-rating-explorer-section",
    name: "Ratings",
    icon: "star-half-o",
    description: "Ratings for applications that have been linked to this viewpoint",
    id: 21
};

const relatedMeasurablesSection = {
    componentId: "related-measurables-section",
    name: "Viewpoints",
    icon: "link",
    description: "Change initiative, viewpoint and app group relationships involving this entity",
    id: 22
};

const relatedDataTypesSection = {
    componentId: "related-data-type-section",
    name: "Types",
    icon: "qrcode",
    description: "Data types with a direct relationship to this entity",
    id: 23
};

const relatedAppsSection = {
    componentId: "related-apps-section",
    name: "Apps",
    icon: "desktop",
    description: "Applications with a direct relationship to this entity",
    id: 24
};

const relatedAppGroupsSection = {
    componentId: "related-app-groups-section",
    name: "Groups",
    icon: "object-group",
    description: "Application groups with a direct relationship to this entity",
    id: 25
};

const personHierarchySection = {
    componentId: "person-hierarchy-section",
    name: "Hierarchy",
    icon: "address-book-o",
    description: "Managers and direct reports of this person",
    id: 26
};

const personAppsSection = {
    componentId: "person-apps-section",
    name: "Applications",
    icon: "desktop",
    description: "Applications that are related to this person by an involvement kind",
    id: 27
};

const dataTypeOriginatorsSection = {
    componentId: "data-type-originators",
    name: "Originators",
    icon: "inbox",
    description: "Applications which are a source of the parent datatype",
    id: 28
};

const dataTypeFlowSection = {
    componentId: "data-type-flow-section",
    name: "Data Flows",
    icon: "random",
    id: 29 //unused?
};

const relatedPhysicalFieldSection = {
    componentId: "related-physical-spec-defn-field-section",
    name: "Physical Fields",
    icon: "file-code-o",
    description: "Physical specification definition fields",
    id: 40
};

const relatedOrgUnitSection = {
    componentId: "related-org-unit-section",
    name: "Organisation Units",
    icon: "sitemap",
    description: "Organisational units with a direct relationship to this entity",
    id: 50
};

const logicalDataElementsSection = {
    componentId: "logical-data-elements-section",
    name: "Logical Data Elements",
    icon: "asterisk",
    description: "Logical data elements related to this entity",
    id: 60
};

const physicalFlowParticipantsSection = {
    componentId: "physical-flow-participants-section",
    name: "Flow Participants",
    icon: "cubes",
    description: "Servers related to applications involved in this flow",
    id: 70
};

const changeSetSection = {
    componentId: "change-set-section",
    name: "Change Sets",
    icon: "hourglass-2",
    description: "Change sets for which this entity is the parent",
    id: 80
};

const personChangeSetSection = {
    componentId: "person-change-set-section",
    name: "Change Sets",
    icon: "hourglass-2",
    description: "Direct involvements with a change set and indirect involvements inferred via the entities involved in the change unit",
    id: 90
};

const licenceSection = {
    componentId: "licence-section",
    name: "Licences",
    icon: "id-card-o",
    description: "Open source licences used by this software package",
    id: 100
};

const flowSpecDefinitionSection = {
    componentId: "flow-spec-definition-section",
    name: "Specification Definition",
    icon: "file-code-o",
    description: "Specification definitions related to this flow",
    id: 110
};

const attestationSummarySection = {
    componentId: "attestation-summary-section",
    name: "Attestations",
    icon: "check-square-o",
    description: "Summary of attestations for logical flows, physical flows and viewpoints against applications related to this entity",
    id: 120
};

const softwarePackageVersions = {
    componentId: "software-package-versions",
    name: "Versions",
    icon: "clock-o",
    description: "Software package versions used by this entity and their vulnerability counts",
    id: 130
};

const softwarePackagesSection = {
    componentId: "software-packages-section",
    name: "Software Packages",
    icon: "gift",
    description: "Software packages and the applications using them that are related to this entity",
    id: 140
};

const orgUnitDirectMeasurableSection = {
    componentId: "org-unit-direct-measurable-section",
    name: "Direct Viewpoints",
    icon: "puzzle-piece",
    description: "Viewpoints which have been directly associated to this entity",
    id: 150
};

const personMeasurableInvolvementsSection = {
    componentId: "person-measurable-involvements-section",
    name: "Viewpoint Involvements",
    icon: "user-o",
    description: "Viewpoints this person, or one of their reportees, has an involvement with",
    id: 160
};

const reportGridViewSection = {
    componentId: "report-grid-view-section",
    name: "Report Grids",
    icon: "cloud",
    description: "Reports composed of viewpoint ratings, assessment ratings and costs for applications related to this entity",
    id: 170
};


const appCostsSummarySection = {
    componentId: "app-costs-summary-section",
    name: "Application Costs",
    icon: "money",
    description: "Cost summaries based upon applications related to this entity",
    id: 180
};

const entityCostsSection = {
    componentId: "entity-costs-section",
    name: "Costs",
    icon: "money",
    id: 190
};

const assessmentRatingSection = {
    componentId: "assessment-rating-section",
    name: "Assessment Ratings",
    icon: "puzzle-piece",
    description: "assessments related to this entity",
    id: 200
};

const physicalFlowSection = {
    componentId: "physical-flow-section",
    name: "Physical Flows",
    icon: "qrcode",
    description: "Physical flows related to this entity",
    id: 210
};

const changeUnitSection = {
    componentId: "change-unit-section",
    name: "Change Units",
    icon: "hourglass-2",
    id: 220
};

const specificationDefinitionSection = {
    componentId: "physical-spec-definition-section",
    name: "Specification Definitions",
    icon: "file-code-o",
    description: "Specification definitions that describe this physical specification",
    id: 230
};

const appComplexitySummarySection = {
    componentId: "app-complexity-summary-section",
    name: "Application Complexity",
    icon: "sort-numeric-asc",
    description: "Complexity summaries based upon applications related to this entity",
    id: 240
};

const companionAppRulesSection = {
    componentId: "companion-app-rules-section",
    name: "Companion App Rules",
    icon: "desktop",
    description: "Flow classification rules that share the same source application",
    id: 250
};

const companionDataTypeRulesSection = {
    componentId: "companion-data_type-rules-section",
    name: "Companion Data Type Rules",
    icon: "qrcode",
    description: "Flow classification rules that share the same data type or one of its parents",
    id: 260
};

const entityAttestationSection = {
    componentId: "attestation-section",
    name: "Attestations",
    icon: "check-square-o",
    description: "Logical and physical flow attestations and viewpoint rating attestations for this application",
    id: 10001
};

const legalEntitySection = {
    // svelteComponent: LegalEntitySection,
    componentId: "legal-entity-section",
    name: "Legal Entity Relationships",
    icon: "building-o",
    description: "Legal Entity relationships related to this entity",
    id: 10010,
};


const legalEntityRelationshipKindSection = {
    // svelteComponent: LegalEntityRelationshipSection,
    componentId: "legal-entity-relationship-kind-section",
    name: "Relationships",
    icon: "link",
    description: "Relationships between legal entities and other waltz entities of this relationship kind",
    id: 10020,
};


const taxonomyChangesSection = {
    componentId: "taxonomy-changes-section",
    name: "Taxonomy Changes",
    icon: "cog",
    description: " This section lists any formal changes against this taxonomy. These changes are ones that have been defined and applied in Waltz.",
    id: 10030,
};


const dataTypeDecoratorSection = {
    componentId: "data-type-decorator-section",
    name: "Data Types",
    icon: "qrcode",
    description: "Data Types decorating this entity",
    id: 10040,
};

const involvementsSection = {
    svelteComponent: InvolvementsSection,
    componentId: "involvements-section",
    name: "Involvements",
    icon: "users",
    description: "Involvements for this involvement kind",
    id: 10050,
};


export const dynamicSections = {
    appCostsSection,
    appCostsSummarySection,
    appComplexitySummarySection,
    appsSection,
    assessmentRatingSection,
    attestationSummarySection,
    flowClassificationRulesSection,
    bookmarksSection,
    changeInitiativeSection,
    changeLogSection,
    changeSetSection,
    changeUnitSection,
    companionAppRulesSection,
    companionDataTypeRulesSection,
    dataFlowSection,
    dataTypeDecoratorSection,
    dataTypeFlowSection,
    dataTypeOriginatorsSection,
    entityAttestationSection,
    entityCostsSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    entityStatisticSection,
    entityStatisticSummarySection,
    flowSpecDefinitionSection,
    involvedPeopleSection,
    involvementsSection,
    legalEntitySection,
    legalEntityRelationshipKindSection,
    licenceSection,
    logicalDataElementsSection,
    logicalFlowsTabgroupSection,
    measurableRatingAppSection,
    measurableRatingExplorerSection,
    measurableRatingsBrowserSection,
    measurableRatingGridView: reportGridViewSection,
    orgUnitDirectMeasurableSection,
    personAppsSection,
    personChangeSetSection,
    personMeasurableInvolvementsSection,
    personHierarchySection,
    physicalFlowSection,
    physicalFlowParticipantsSection,
    relatedAppGroupsSection,
    relatedAppsSection,
    relatedDataTypesSection,
    relatedMeasurablesSection,
    relatedOrgUnitSection,
    relatedPhysicalFieldSection,
    softwarePackagesSection,
    softwarePackageVersions,
    specificationDefinitionSection,
    surveySection,
    taxonomyChangesSection,
    technologySection,
    technologySummarySection
};


function pack(section, children = []) {
    return children
        ? Object.assign({}, section, { children })
        : section;
}


const appSections = [
    assessmentRatingSection,
    bookmarksSection,
    changeInitiativeSection,
    changeSetSection,
    dataFlowSection,
    entityAttestationSection,
    entityCostsSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    entityStatisticSection,
    involvedPeopleSection,
    legalEntitySection,
    measurableRatingAppSection,
    surveySection,
    technologySection,
    changeLogSection
];

const actorSections = [
    appsSection,
    assessmentRatingSection,
    bookmarksSection,
    changeInitiativeSection,
    dataFlowSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    technologySummarySection,
    changeLogSection
];

const changeInitiativeSections = [
    assessmentRatingSection,
    bookmarksSection,
    changeSetSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    relatedMeasurablesSection,
    pack(relatedAppsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             entityStatisticSummarySection,
             technologySummarySection,
             logicalFlowsTabgroupSection
         ]),
    relatedAppGroupsSection,
    relatedDataTypesSection,
    surveySection,
    changeLogSection
];

const orgUnitSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             attestationSummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    entityDiagramsSection,
    bookmarksSection,
    changeInitiativeSection,
    changeSetSection,
    involvedPeopleSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    legalEntitySection,
    orgUnitDirectMeasurableSection,
    changeLogSection
];

const measurableSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             entityStatisticSummarySection,
             reportGridViewSection,
             technologySummarySection
         ]),
    bookmarksSection,
    assessmentRatingSection,
    changeSetSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    legalEntitySection,
    measurableRatingExplorerSection,
    relatedMeasurablesSection,
    relatedDataTypesSection,
    changeLogSection
];


const measurableRatingSections = [
    bookmarksSection,
    assessmentRatingSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    changeLogSection
];


const personSections = [
    bookmarksSection,
    changeInitiativeSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    pack(personAppsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             attestationSummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    entityDiagramsSection,
    personChangeSetSection,
    personHierarchySection,
    personMeasurableInvolvementsSection,
    legalEntitySection,
    surveySection,
    changeLogSection
];

const dataTypeSections = [
    appsSection,
    bookmarksSection,
    dataTypeOriginatorsSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    logicalDataElementsSection,
    reportGridViewSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    changeLogSection
];

const appGroupSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             attestationSummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    bookmarksSection,
    entityDiagramsSection,
    changeInitiativeSection,
    changeSetSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    legalEntitySection,
    relatedAppGroupsSection,
    relatedDataTypesSection,
    relatedMeasurablesSection,
    changeLogSection
];

const appListSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             attestationSummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    bookmarksSection,
    entityDiagramsSection,
    changeInitiativeSection,
    changeSetSection,
    entityNamedNotesSection,
    pack(logicalFlowsTabgroupSection, [flowClassificationRulesSection]),
    legalEntitySection,
    relatedDataTypesSection,
    relatedMeasurablesSection,
    changeLogSection
];

const scenarioSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             entityStatisticSummarySection,
             technologySummarySection
         ]),
    bookmarksSection,
    changeInitiativeSection,
    entityNamedNotesSection,
    logicalFlowsTabgroupSection,
    changeLogSection
];

const flowDiagramSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    entityDiagramsSection,
    bookmarksSection,
    entityNamedNotesSection,
    changeLogSection
];


const involvementKindSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvementsSection,
    changeLogSection
];

const processDiagramSections = [
    pack(appsSection,
         [
             appCostsSummarySection,
             appComplexitySummarySection,
             attestationSummarySection,
             entityStatisticSummarySection,
             measurableRatingsBrowserSection,
             reportGridViewSection,
             technologySummarySection
         ]),
    entityDiagramsSection,
    bookmarksSection,
    entityNamedNotesSection,
    changeLogSection
];

const physicalFlowSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    flowSpecDefinitionSection,
    involvedPeopleSection,
    physicalFlowParticipantsSection,
    changeLogSection
];

const logicalDataFlowSections = [
    assessmentRatingSection,
    bookmarksSection,
    dataTypeDecoratorSection,
    entityDiagramsSection,
    entityNamedNotesSection,
    physicalFlowSection,
    changeLogSection
];

const changeSetSections = [
    assessmentRatingSection,
    bookmarksSection,
    changeUnitSection,
    involvedPeopleSection,
    changeLogSection
];

const licenceSections = [
    appsSection,
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    softwarePackagesSection,
    changeLogSection
];

const softwarePackageSections = [
    appsSection,
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    entityStatisticSection,
    licenceSection,
    softwarePackageVersions,
    changeLogSection
];

const physicalSpecificationSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityDiagramsSection,
    physicalFlowSection,
    specificationDefinitionSection,
    changeLogSection
];

const entityRelationshipSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    changeLogSection
];

const databaseSections = [
    appsSection,
    entityNamedNotesSection,
    bookmarksSection,
    changeLogSection,
];

const flowClassificationRuleSections = [
    assessmentRatingSection,
    bookmarksSection,
    companionAppRulesSection,
    companionDataTypeRulesSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    changeLogSection
];

const legalEntitySections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    legalEntitySection,
    changeLogSection
];

const legalEntityRelationshipKindSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    legalEntityRelationshipKindSection,
    changeLogSection
];

const legalEntityRelationshipSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    changeLogSection
];

const endUserApplicationSections = [
    assessmentRatingSection,
    bookmarksSection,
    entityNamedNotesSection,
    involvedPeopleSection,
    dataFlowSection,
    entityDiagramsSection,
    changeLogSection
];


export const dynamicSectionsByKind = {
    "main.actor.view": actorSections,
    "main.app-group.view": appGroupSections,
    "main.app.asset-code": appSections,
    "main.app.external-id": appSections,
    "main.app.view": appSections,
    "main.app": appListSections,
    "main.change-initiative.external-id": changeInitiativeSections,
    "main.change-initiative.view": changeInitiativeSections,
    "main.change-set.view": changeSetSections,
    "main.data-type.code": dataTypeSections,
    "main.data-type.external-id": dataTypeSections,
    "main.data-type.view": dataTypeSections,
    "main.database.external-id": databaseSections,
    "main.database.view": databaseSections,
    "main.entity-relationship.view": entityRelationshipSections,
    "main.end-user-application.view": endUserApplicationSections,
    "main.flow-classification-rule.view": flowClassificationRuleSections,
    "main.flow-diagram.view": flowDiagramSections,
    "main.involvement-kind.view": involvementKindSections,
    "main.legal-entity.view": legalEntitySections,
    "main.legal-entity-relationship.view": legalEntityRelationshipSections,
    "main.legal-entity-relationship-kind.view": legalEntityRelationshipKindSections,
    "main.licence.external-id": licenceSections,
    "main.licence.view": licenceSections,
    "main.logical-flow.view": logicalDataFlowSections,
    "main.measurable.view": measurableSections,
    "main.measurable-rating.view": measurableRatingSections,
    "main.org-unit.view": orgUnitSections,
    "main.person.id": personSections,
    "main.person.userId": personSections,
    "main.person.view": personSections,
    "main.physical-flow.view": physicalFlowSections,
    "main.physical-specification.view": physicalSpecificationSections,
    "main.process-diagram.view": processDiagramSections,
    "main.scenario.view": scenarioSections,
    "main.software-package.view": softwarePackageSections
};
