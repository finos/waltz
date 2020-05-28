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

package com.khartec.waltz.model;

public enum EntityKind {

    ACTOR("Actor"),
    ALLOCATION_SCHEME("Allocation scheme"),
    APPLICATION("Application"),
    APP_GROUP("Application group"),
    ASSET_COST("Asset cost"),
    ATTESTATION("Attestation"),
    ATTESTATION_RUN("Attestation run"),
    AUTHORITATIVE_SOURCE("Authoritative source"),
    BOOKMARK("Bookmark"),
    CHANGE_INITIATIVE("Change initiative"),
    CHANGE_SET("Change set"),
    CHANGE_UNIT("Change unit"),
    DATABASE("Database"),
    DATA_TYPE("Datatype"),
    END_USER_APPLICATION("End user application"),
    ENTITY_HIERARCHY("Entity hierarchy"),
    ENTITY_NAMED_NOTE("Entity named note"),
    ENTITY_NAMED_NOTE_TYPE("Entity named note type"),
    ENTITY_STATISTIC("Entity statistic"),
    FLOW_DIAGRAM("Flow diagram"),
    FLOW_ANNOTATION("Flow annotation"),
    INVOLVEMENT("Involvement"),
    INVOLVEMENT_KIND("Involvement kind"),
    LICENCE("Licence"),
    LOGICAL_DATA_ELEMENT("Logical data element"),
    LOGICAL_DATA_FLOW("Logical flow"),
    MEASURABLE("Measurable"),
    MEASURABLE_CATEGORY("Measurable category"),
    MEASURABLE_RATING("Measurable rating"),
    MEASURABLE_RATING_PLANNED_DECOMMISSION("Measurable rating planned decommission"),
    MEASURABLE_RATING_REPLACEMENT("Measurable rating replacement"),
    ORG_UNIT("Organisational unit"),
    PERFORMANCE_METRIC_PACK("Performance metric pack"),
    PERSON("Person"),
    PHYSICAL_SPECIFICATION("Physical specification"),
    PHYSICAL_SPEC_DEFN("Physical spec definition"),
    PHYSICAL_SPEC_DEFN_FIELD("Physical spec definition field"),
    PHYSICAL_FLOW("Physical flow"),
    RELATIONSHIP_KIND("Relationship Kind"),
    ROADMAP("Roadmap"),
    SCENARIO("Scenario"),
    SERVER("Server"),
    SOFTWARE("Software"),
    SOFTWARE_VERSION("Software version"),
    SURVEY_INSTANCE("Survey instance"),
    SURVEY_RUN("Survey run"),
    SURVEY_TEMPLATE("Survey template"),
    SYSTEM("System"),
    TAG("Tag"),

    @Deprecated
    CAPABILITY("Capability")  // TO BE REMOVED IN 1.5
    ;

    private final String prettyName;


    EntityKind(String prettyName){

        this.prettyName = prettyName;

    }

    public String prettyName(){ return prettyName; }

}
