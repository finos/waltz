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

export const entity = {
    ACTOR: {
        key: "ACTOR",
        name: "Actor",
        icon: "user-circle",
        description: null,
        position: 100
    },
    AGGREGATE_OVERLAY_DIAGRAM: {
        key: "AGGREGATE_OVERLAY_DIAGRAM",
        name: "Aggregate Overlay Diagram",
        icon: "object-group",
        description: null,
        position: 150
    },
    AGGREGATE_OVERLAY_DIAGRAM_INSTANCE: {
        key: "AGGREGATE_OVERLAY_DIAGRAM_INSTANCE",
        name: "Aggregate Overlay Diagram Instance",
        icon: "object-group",
        description: null,
        position: 160
    },
    ALLOCATION_SCHEME: {
        key: "ALLOCATION_SCHEME",
        name: "Allocation Scheme",
        icon: null,
        description: null,
        position: 200
    },
    APP_CAPABILITY: {
        key: "APP_CAPABILITY",
        name: "Application Function",
        icon: "puzzle-piece",
        description: null,
        position: 210
    },
    APP_GROUP: {
        key: "APP_GROUP",
        name: "Application Group",
        icon: "object-group",
        description: null,
        position: 300
    },
    APP_RATING: {
        key: "APP_RATING",
        name: "Application Rating",
        icon: "star-o",
        description: null,
        position: 400
    },
    APPLICATION: {
        key: "APPLICATION",
        name: "Application",
        icon: "desktop",
        description: null,
        position: 500
    },
    ASSET_COST: {
        key: "ASSET_COST",
        name: "Asset Cost",
        icon: "money",
        description: null,
        position: 600
    },
    ASSESSMENT_DEFINITION: {
        key: "ASSESSMENT_DEFINITION",
        name: "Assessment Definition",
        icon: "puzzle-piece",
        description: null,
        position: 620
    },
    ASSESSMENT_RATING: {
        key: "ASSESSMENT_RATING",
        name: "Assessment Rating",
        icon: "puzzle-piece",
        description: null,
        position: 630
    },
    ATTESTATION: {
        key: "ATTESTATION",
        name: "Attestation",
        icon: "check-square-o",
        description: null,
        position: 650
    },
    ATTESTATION_RUN: {
        key: "ATTESTATION_RUN",
        name: "Attestation Run",
        icon: "check-square-o",
        description: null,
        position: 670
    },
    BOOKMARK: {
        key: "BOOKMARK",
        name: "Bookmark",
        icon: "bookmark-o",
        description: null,
        position: 800
    },
    CHANGE_INITIATIVE: {
        key: "CHANGE_INITIATIVE",
        name: "Change Initiative",
        icon: "paper-plane-o",
        description: null,
        position: 900
    },
    CHANGE_SET: {
        key: "CHANGE_SET",
        name: "Change Set",
        icon: "hourglass-2",
        description: null,
        position: 950
    },
    CHANGE_UNIT: {
        key: "CHANGE_UNIT",
        name: "Change Unit",
        icon: "hourglass-2",
        description: null,
        position: 960
    },
    COST_KIND: {
        key: "COST_KIND",
        name: "Cost Kind",
        icon: "money",
        description: null,
        position: 970
    },
    COMPLEXITY_KIND: {
        key: "COMPLEXITY_KIND",
        name: "Complexity Kind",
        icon: "sort-numeric-asc",
        description: null,
        position: 980
    },
    DATABASE: {
        key: "DATABASE",
        name: "Database",
        icon: "database",
        description: null,
        position: 1000
    },
    DATA_TYPE: {
        key: "DATA_TYPE",
        name: "Data Type",
        icon: "qrcode",
        description: null,
        position: 1100
    },
    END_USER_APPLICATION: {
        key: "END_USER_APPLICATION",
        name: "End User App",
        icon: "table",
        description: null,
        position: 1200
    },
    ENTITY_ALIAS: {
        key: "ENTITY_ALIAS",
        name: "Entity Alias",
        icon: "id-badge",
        description: null,
        position: 1250
    },
    ENTITY_RELATIONSHIP: {
        key: "ENTITY_RELATIONSHIP",
        name: "Entity Relationship",
        icon: "link",
        description: null,
        position: 1260
    },
    ENTITY_STATISTIC: {
        key: "ENTITY_STATISTIC",
        name: "Statistic",
        icon: "pie-chart",
        description: null,
        position: 1300
    },
    EXTERNAL_IDENTIFIER: {
        key: "EXTERNAL_IDENTIFIER",
        name: "External Identifier",
        icon: "comments-o",
        description: null,
        position: 1350
    },
    FLOW_DIAGRAM: {
        key: "FLOW_DIAGRAM",
        name: "Flow Diagram",
        icon: "picture-o",
        description: null,
        position: 1400
    },
    FLOW_CLASSIFICATION_RULE: {
        key: "FLOW_CLASSIFICATION_RULE",
        name: "Flow Classification Rule",
        icon: "shield",
        description: null,
        position: 1450
    },
    INVOLVEMENT: {
        key: "INVOLVEMENT",
        name: "Involvement",
        icon: "share-alt-square",
        description: null,
        position: 1500
    },
    INVOLVEMENT_KIND: {
        key: "INVOLVEMENT_KIND",
        name: "Involvement Kind",
        icon: "share-alt-square",
        description: null,
        position: 1520
    },
    LEGAL_ENTITY: {
        key: "LEGAL_ENTITY",
        name: "Legal Entity",
        icon: "building-o",
        description: null,
        position: 1525
    },
    LICENCE: {
        key: "LICENCE",
        name: "Licence",
        icon: "id-card-o",
        description: null,
        position: 1530
    },
    LOGICAL_DATA_ELEMENT: {
        key: "LOGICAL_DATA_ELEMENT",
        name: "Logical Data Element",
        icon: "asterisk",
        description: null,
        position: 1550
    },
    LOGICAL_DATA_FLOW: {
        key: "LOGICAL_DATA_FLOW",
        name: "Logical Data Flow",
        icon: "random",
        description: null,
        position: 1600
    },
    ORG_UNIT: {
        key: "ORG_UNIT",
        name: "Org Unit",
        icon: "sitemap",
        description: null,
        position: 1700
    },
    MEASURABLE: {
        key: "MEASURABLE",
        name: "Viewpoint",
        icon: "puzzle-piece",
        description: null,
        position: 1800
    },
    MEASURABLE_CATEGORY: {
        key: "MEASURABLE_CATEGORY",
        name: "Viewpoint Category",
        icon: "puzzle-piece",
        description: null,
        position: 1850
    },
    MEASURABLE_RATING: {
        key: "MEASURABLE_RATING",
        name: "Viewpoint Rating",
        icon: undefined,
        description: null,
        position: 1900
    },
    MEASURABLE_RATING_PLANNED_DECOMMISSION: {
        key: "MEASURABLE_RATING_PLANNED_DECOMMISSION",
        name: "Viewpoint Rating Decommission",
        icon: undefined,
        description: null,
        position: 1950
    },
    MEASURABLE_RATING_REPLACEMENT: {
        key: "MEASURABLE_RATING_REPLACEMENT",
        name: "Viewpoint Rating Replacement Application",
        icon: undefined,
        description: null,
        position: 1960
    },
    PERSON: {
        key: "PERSON",
        name: "Person",
        icon: "user",
        description: null,
        position: 2000
    },
    PHYSICAL_SPECIFICATION: {
        key: "PHYSICAL_SPECIFICATION",
        name: "Physical Specification",
        icon: "file-text",
        description: null,
        position: 2100
    },
    PHYSICAL_FLOW: {
        key: "PHYSICAL_FLOW",
        name: "Physical Flow",
        icon: "qrcode",
        description: null,
        position: 2200
    },
    PROCESS: {
        key: "PROCESS",
        name: "Process",
        icon: "code-fork",
        description: null,
        position: 2300
    },
    PROCESS_DIAGRAM: {
        key: "PROCESS_DIAGRAM",
        name: "Process Diagram",
        icon: "cogs",
        description: null,
        position: 2310
    },
    REPORT_GRID: {
        key: "REPORT_GRID",
        name: "Report Grid",
        icon: "table",
        description: null,
        position: 2315
    },
    REPORT_GRID_DERIVED_COLUMN_DEFINITION: {
        key: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
        name: "Report Grid Derived Column Definition",
        icon: "cubes",
        description: null,
        position: 2320
    },
    REPORT_GRID_FIXED_COLUMN_DEFINITION: {
        key: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
        name: "Report Grid Fixed Column Definition",
        icon: "cube",
        description: null,
        position: 2330
    },
    ROADMAP: {
        key: "ROADMAP",
        name: "Roadmap",
        icon: "road",
        description: null,
        position: 2350
    },
    ROLE: {
        key: "ROLE",
        name: "Role",
        icon: "key",
        description: null,
        position: 2360
    },
    SCENARIO: {
        key: "SCENARIO",
        name: "Scenario",
        icon: "picture-o",
        description: null,
        position: 2370
    },
    SERVER: {
        key: "SERVER",
        name: "Server",
        icon: "server",
        description: null,
        position: 2400
    },
    SOFTWARE: {
        key: "SOFTWARE",
        name: "Software",
        icon: "gift",
        description: null,
        position: 2500
    },
    SURVEY: {
        key: "SURVEY",
        name: "Survey",
        icon: "wpforms",
        description: null,
        position: 2550
    },
    SURVEY_INSTANCE: {
        key: "SURVEY_INSTANCE",
        name: "Survey Instance",
        icon: "wpforms",
        description: null,
        position: 2570
    },
    SURVEY_QUESTION: {
        key: "SURVEY_QUESTION",
        name: "Survey Question",
        icon: "question-circle-o",
        description: null,
        position: 2580
    },
    SURVEY_TEMPLATE: {
        key: "SURVEY_TEMPLATE",
        name: "Survey Template",
        icon: "wpforms",
        description: null,
        position: 2590
    },
    SURVEY_INSTANCE_OWNER: {
        key: "SURVEY_INSTANCE_OWNER",
        name: "Survey Instance Owner",
        icon: "user",
        description: null,
        position: 2593
    },
    SURVEY_INSTANCE_RECIPIENT: {
        key: "SURVEY_INSTANCE_RECIPIENT",
        name: "Survey Instance Recipient",
        icon: "users",
        description: null,
        position: 2596
    },
    SYSTEM: {
        key: "SYSTEM",
        name: "System",
        icon: "gears",
        description: null,
        position: 2600
    },
    TAG: {
        key: "TAG",
        name: "Tag",
        icon: "tags",
        description: null,
        position: 2700
    }
};