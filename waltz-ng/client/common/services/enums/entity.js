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
        position: 10
    },
    AGGREGATE_OVERLAY_DIAGRAM: {
        key: "AGGREGATE_OVERLAY_DIAGRAM",
        name: "Aggregate Overlay Diagram",
        icon: "object-group",
        description: null,
        position: 15
    },
    AGGREGATE_OVERLAY_DIAGRAM_INSTANCE: {
        key: "AGGREGATE_OVERLAY_DIAGRAM_INSTANCE",
        name: "Aggregate Overlay Diagram Instance",
        icon: "object-group",
        description: null,
        position: 16
    },
    ALLOCATION_SCHEME: {
        key: "ALLOCATION_SCHEME",
        name: "Allocation Scheme",
        icon: null,
        description: null,
        position: 20
    },
    APP_CAPABILITY: {
        key: "APP_CAPABILITY",
        name: "Application Function",
        icon: "puzzle-piece",
        description: null,
        position: 20
    },
    APP_GROUP: {
        key: "APP_GROUP",
        name: "Application Group",
        icon: "object-group",
        description: null,
        position: 30
    },
    APP_RATING: {
        key: "APP_RATING",
        name: "Application Rating",
        icon: "star-o",
        description: null,
        position: 40
    },
    APPLICATION: {
        key: "APPLICATION",
        name: "Application",
        icon: "desktop",
        description: null,
        position: 50
    },
    ASSET_COST: {
        key: "ASSET_COST",
        name: "Asset Cost",
        icon: "money",
        description: null,
        position: 60
    },
    ASSESSMENT_DEFINITION: {
        key: "ASSESSMENT_DEFINITION",
        name: "Assessment Definition",
        icon: "puzzle-piece",
        description: null,
        position: 62
    },
    ASSESSMENT_RATING: {
        key: "ASSESSMENT_RATING",
        name: "Assessment Rating",
        icon: "puzzle-piece",
        description: null,
        position: 63
    },
    ATTESTATION: {
        key: "ATTESTATION",
        name: "Attestation",
        icon: "check-square-o",
        description: null,
        position: 65
    },
    ATTESTATION_RUN: {
        key: "ATTESTATION_RUN",
        name: "Attestation Run",
        icon: "check-square-o",
        description: null,
        position: 67
    },
    BOOKMARK: {
        key: "BOOKMARK",
        name: "Bookmark",
        icon: "bookmark-o",
        description: null,
        position: 80
    },
    CHANGE_INITIATIVE: {
        key: "CHANGE_INITIATIVE",
        name: "Change Initiative",
        icon: "paper-plane-o",
        description: null,
        position: 90
    },
    CHANGE_SET: {
        key: "CHANGE_SET",
        name: "Change Set",
        icon: "hourglass-2",
        description: null,
        position: 95
    },
    CHANGE_UNIT: {
        key: "CHANGE_UNIT",
        name: "Change Unit",
        icon: "hourglass-2",
        description: null,
        position: 96
    },
    COST_KIND: {
        key: "COST_KIND",
        name: "Cost Kind",
        icon: "money",
        description: null,
        position: 97
    },
    COMPLEXITY_KIND: {
        key: "COMPLEXITY_KIND",
        name: "Complexity Kind",
        icon: "sort-numeric-asc",
        description: null,
        position: 98
    },
    DATABASE: {
        key: "DATABASE",
        name: "Database",
        icon: "database",
        description: null,
        position: 100
    },
    DATA_TYPE: {
        key: "DATA_TYPE",
        name: "Data Type",
        icon: "qrcode",
        description: null,
        position: 110
    },
    END_USER_APPLICATION: {
        key: "END_USER_APPLICATION",
        name: "End User App",
        icon: "table",
        description: null,
        position: 120
    },
    ENTITY_ALIAS: {
        key: "ENTITY_ALIAS",
        name: "Entity Alias",
        icon: "id-badge",
        description: null,
        position: 125
    },
    ENTITY_RELATIONSHIP: {
        key: "ENTITY_RELATIONSHIP",
        name: "Entity Relationship",
        icon: "link",
        description: null,
        position: 125
    },
    ENTITY_STATISTIC: {
        key: "ENTITY_STATISTIC",
        name: "Statistic",
        icon: "pie-chart",
        description: null,
        position: 130
    },
    EXTERNAL_IDENTIFIER: {
        key: "EXTERNAL_IDENTIFIER",
        name: "External Identifier",
        icon: "comments-o",
        description: null,
        position: 135
    },
    FLOW_DIAGRAM: {
        key: "FLOW_DIAGRAM",
        name: "Flow Diagram",
        icon: "picture-o",
        description: null,
        position: 140
    },
    FLOW_CLASSIFICATION_RULE: {
        key: "FLOW_CLASSIFICATION_RULE",
        name: "Flow Classification Rule",
        icon: "shield",
        description: null,
        position: 145
    },
    INVOLVEMENT: {
        key: "INVOLVEMENT",
        name: "Involvement",
        icon: "share-alt-square",
        description: null,
        position: 150
    },
    INVOLVEMENT_KIND: {
        key: "INVOLVEMENT_KIND",
        name: "Involvement Kind",
        icon: "share-alt-square",
        description: null,
        position: 150
    },
    LICENCE: {
        key: "LICENCE",
        name: "Licence",
        icon: "id-card-o",
        description: null,
        position: 153
    },
    LOGICAL_DATA_ELEMENT: {
        key: "LOGICAL_DATA_ELEMENT",
        name: "Logical Data Element",
        icon: "asterisk",
        description: null,
        position: 155
    },
    LOGICAL_DATA_FLOW: {
        key: "LOGICAL_DATA_FLOW",
        name: "Logical Data Flow",
        icon: "random",
        description: null,
        position: 160
    },
    ORG_UNIT: {
        key: "ORG_UNIT",
        name: "Org Unit",
        icon: "sitemap",
        description: null,
        position: 170
    },
    MEASURABLE: {
        key: "MEASURABLE",
        name: "Viewpoint",
        icon: "puzzle-piece",
        description: null,
        position: 180
    },
    MEASURABLE_CATEGORY: {
        key: "MEASURABLE_CATEGORY",
        name: "Viewpoint Rating",
        icon: "puzzle-piece",
        description: null,
        position: 185
    },
    MEASURABLE_RATING: {
        key: "MEASURABLE_RATING",
        name: "Viewpoint Rating",
        icon: undefined,
        description: null,
        position: 190
    },
    MEASURABLE_RATING_PLANNED_DECOMMISSION: {
        key: "MEASURABLE_RATING_PLANNED_DECOMMISSION",
        name: "Viewpoint Rating Decommission",
        icon: undefined,
        description: null,
        position: 195
    },
    MEASURABLE_RATING_REPLACEMENT: {
        key: "MEASURABLE_RATING_REPLACEMENT",
        name: "Viewpoint Rating Replacement Application",
        icon: undefined,
        description: null,
        position: 196
    },
    PERSON: {
        key: "PERSON",
        name: "Person",
        icon: "user",
        description: null,
        position: 200
    },
    PHYSICAL_SPECIFICATION: {
        key: "PHYSICAL_SPECIFICATION",
        name: "Physical Specification",
        icon: "file-text",
        description: null,
        position: 210
    },
    PHYSICAL_FLOW: {
        key: "PHYSICAL_FLOW",
        name: "Physical Flow",
        icon: "qrcode",
        description: null,
        position: 220
    },
    PROCESS: {
        key: "PROCESS",
        name: "Process",
        icon: "code-fork",
        description: null,
        position: 230
    },
    PROCESS_DIAGRAM: {
        key: "PROCESS_DIAGRAM",
        name: "Process Diagram",
        icon: "cogs",
        description: null,
        position: 231
    },
    REPORT_GRID: {
        key: "REPORT_GRID",
        name: "Report Grid",
        icon: "table",
        description: null,
        position: 231
    },
    REPORT_GRID_DERIVED_COLUMN_DEFINITION: {
        key: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
        name: "Report Grid Derived Column Definition",
        icon: "cubes",
        description: null,
        position: 232
    },
    REPORT_GRID_FIXED_COLUMN_DEFINITION: {
        key: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
        name: "Report Grid Fixed Column Definition",
        icon: "cube",
        description: null,
        position: 233
    },
    ROADMAP: {
        key: "ROADMAP",
        name: "Roadmap",
        icon: "road",
        description: null,
        position: 235
    },
    ROLE: {
        key: "ROLE",
        name: "Role",
        icon: "key",
        description: null,
        position: 236
    },
    SCENARIO: {
        key: "SCENARIO",
        name: "Scenario",
        icon: "picture-o",
        description: null,
        position: 237
    },
    SERVER: {
        key: "SERVER",
        name: "Server",
        icon: "server",
        description: null,
        position: 240
    },
    SOFTWARE: {
        key: "SOFTWARE",
        name: "Software",
        icon: "gift",
        description: null,
        position: 250
    },
    SURVEY: {
        key: "SURVEY",
        name: "Survey",
        icon: "wpforms",
        description: null,
        position: 255
    },
    SURVEY_INSTANCE: {
        key: "SURVEY_INSTANCE",
        name: "Survey Instance",
        icon: "wpforms",
        description: null,
        position: 257
    },
    SURVEY_QUESTION: {
        key: "SURVEY_QUESTION",
        name: "Survey Question",
        icon: "question-circle-o",
        description: null,
        position: 258
    },
    SURVEY_TEMPLATE: {
        key: "SURVEY_TEMPLATE",
        name: "Survey Template",
        icon: "wpforms",
        description: null,
        position: 259
    },
    SYSTEM: {
        key: "SYSTEM",
        name: "System",
        icon: "gears",
        description: null,
        position: 260
    },
    TAG: {
        key: "TAG",
        name: "Tag",
        icon: "tags",
        description: null,
        position: 270
    }
};