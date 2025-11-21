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

package org.finos.waltz.model;

public enum EntityKind {

    ALL("All"), // when used in an entity ref the id will be ignored
    ACTOR("Actor"),
    AGGREGATE_OVERLAY_DIAGRAM("Aggregate Overlay Diagram"),
    AGGREGATE_OVERLAY_DIAGRAM_INSTANCE("Aggregate Overlay Diagram Instance"),
    ALLOCATION("Allocation"),
    ALLOCATION_SCHEME("Allocation scheme"),
    APPLICATION("Application"),
    APP_GROUP("Application group"),
    ASSESSMENT_DEFINITION("Assessment definition"),
    ASSESSMENT_RATING("Assessment rating"),
    ASSET_COST("Asset cost"),
    ATTESTATION("Attestation"),
    ATTESTATION_RUN("Attestation run"),
    BOOKMARK("Bookmark"),
    CHANGE_INITIATIVE("Change initiative"),
    CHANGE_SET("Change set"),
    CHANGE_UNIT("Change unit"),
    COST("Cost"),
    COST_KIND("Cost Kind"),
    COMPLEXITY("Complexity"),
    COMPLEXITY_KIND("Complexity Kind"),
    CUSTOM_ENVIRONMENT("Custom environment"),
    CUSTOM_ENVIRONMENT_USAGE("Custom environment usage"),
    DATABASE("Database"),
    DATABASE_USAGE("Database Usage"),
    DATA_TYPE("Datatype"),
    END_USER_APPLICATION("End user application"),
    ENTITY_ALIAS("Entity alias"),
    ENTITY_FIELD_REFERENCE("Entity field reference"),
    ENTITY_HIERARCHY("Entity hierarchy"),
    ENTITY_NAMED_NOTE("Entity named note"),
    ENTITY_NAMED_NOTE_TYPE("Entity named note type"),
    ENTITY_RELATIONSHIP("Entity relationship"),
    ENTITY_STATISTIC("Entity statistic"),
    EXTERNAL_IDENTIFIER("External Identifier"),
    FLOW_ANNOTATION("Flow annotation"),
    FLOW_CLASSIFICATION_RULE("Flow classification rule"),
    FLOW_CLASSIFICATION("Flow classification"),
    FLOW_DIAGRAM("Flow diagram"),
    INVOLVEMENT("Involvement"),
    INVOLVEMENT_GROUP("Involvement Group"),
    INVOLVEMENT_KIND("Involvement kind"),
    LICENCE("Licence"),
    LEGAL_ENTITY("Legal Entity"),
    LEGAL_ENTITY_RELATIONSHIP("Legal Entity Relationship"),
    LEGAL_ENTITY_RELATIONSHIP_KIND("Legal Entity Relationship Kind"),
    LOGICAL_DATA_ELEMENT("Logical Data Element"),
    LOGICAL_DATA_FLOW("Logical Flow"),
    LOGICAL_FLOW_DECORATOR("Logical Flow Decorator"),
    LOGICAL_DATA_FLOW_DATA_TYPE_DECORATOR("Logical Flow Datatype Decorator"),
    MEASURABLE("Measurable"),
    MEASURABLE_CATEGORY("Measurable category"),
    MEASURABLE_RATING("Measurable rating"),
    MEASURABLE_RATING_PLANNED_DECOMMISSION("Measurable rating planned decommission"),
    MEASURABLE_RATING_REPLACEMENT("Measurable rating replacement"),
    ORG_UNIT("Organisational unit"),
    PERFORMANCE_METRIC_PACK("Performance metric pack"),
    PERMISSION_GROUP("Permission Group"),
    PERSON("Person"),
    PHYSICAL_SPECIFICATION("Physical specification"),
    PHYSICAL_SPEC_DATA_TYPE_DECORATOR("Physical specification data type decorator"),
    PHYSICAL_SPEC_DEFN("Physical spec definition"),
    PHYSICAL_SPEC_DEFN_FIELD("Physical spec definition field"),
    PHYSICAL_FLOW("Physical flow"),
    PROCESS_DIAGRAM("Process Diagram"),
    PROPOSED_FLOW("Proposed Flow"),
    RELATIONSHIP_KIND("Relationship Kind"),
    REPORT_GRID("Report Grid"),
    REPORT_GRID_DERIVED_COLUMN_DEFINITION("Report Grid Derived Column Definition"),
    REPORT_GRID_FIXED_COLUMN_DEFINITION("Report Grid Fixed Column Definition"),
    ROADMAP("Roadmap"),
    ROLE("Role"),
    SCENARIO("Scenario"),
    SERVER("Server"),
    SERVER_USAGE("Server usage"),
    SOFTWARE("Software"),
    SOFTWARE_VERSION("Software version"),
    SURVEY_INSTANCE("Survey instance"),
    SURVEY_INSTANCE_OWNER("Survey instance owner"),
    SURVEY_INSTANCE_RECIPIENT("Survey instance recipient"),
    SURVEY_QUESTION("Survey question"),
    SURVEY_RUN("Survey run"),
    SURVEY_TEMPLATE("Survey template"),
    SYSTEM("System"),
    TAG("Tag"),
    USER_ROLE("User Role"),

    @Deprecated
    CAPABILITY("Capability"),  // TO BE REMOVED IN 1.5

    @Deprecated
    AUTHORITATIVE_SOURCE("Authoritative source"), // TO BE REMOVED IN 1.36
    ;


    private final String prettyName;


    EntityKind(String prettyName) {

        this.prettyName = prettyName;

    }

    public String prettyName() {
        return prettyName;
    }

}
