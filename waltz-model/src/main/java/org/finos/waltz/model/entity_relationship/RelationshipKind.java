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

package org.finos.waltz.model.entity_relationship;

import org.finos.waltz.model.EntityKind;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collections;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.fromArray;
import static org.jooq.lambda.tuple.Tuple.tuple;


public enum RelationshipKind {

    HAS(Collections.emptySet()),

    DEPRECATES(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION),
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.MEASURABLE)
    )),

    PARTICIPATES_IN(fromArray(
            tuple(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE)
    )),

    RELATES_TO(fromArray(
            tuple(EntityKind.APP_GROUP, EntityKind.APP_GROUP),
            tuple(EntityKind.APP_GROUP, EntityKind.CHANGE_INITIATIVE),
            tuple(EntityKind.APP_GROUP, EntityKind.MEASURABLE),
            tuple(EntityKind.APP_GROUP, EntityKind.ROADMAP),
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.CHANGE_INITIATIVE),
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.MEASURABLE),
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.DATA_TYPE),
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APP_GROUP),
            tuple(EntityKind.MEASURABLE, EntityKind.APP_GROUP),
            tuple(EntityKind.MEASURABLE, EntityKind.MEASURABLE),
            tuple(EntityKind.MEASURABLE, EntityKind.CHANGE_INITIATIVE),
            tuple(EntityKind.ORG_UNIT, EntityKind.ROADMAP),
            tuple(EntityKind.LICENCE, EntityKind.APPLICATION)
    )),

    SUPPORTS(fromArray(
            tuple(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE),
            tuple(EntityKind.ACTOR, EntityKind.CHANGE_INITIATIVE)
    )),

    APPLICATION_NEW(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    )),

    APPLICATION_FUNCTIONAL_CHANGE(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    )),

    APPLICATION_DECOMMISSIONED(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    )),

    APPLICATION_NFR_CHANGE(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    )),

    DATA_PUBLISHER(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    )),

    DATA_CONSUMER(fromArray(
            tuple(EntityKind.CHANGE_INITIATIVE, EntityKind.APPLICATION)
    ));


    private Set<Tuple2<EntityKind, EntityKind>> allowedEntityKinds;


    RelationshipKind(Set<Tuple2<EntityKind, EntityKind>> allowedEntityKinds) {
        this.allowedEntityKinds = allowedEntityKinds;
    }


    public Set<Tuple2<EntityKind, EntityKind>> getAllowedEntityKinds() {
        return this.allowedEntityKinds;
    }

}
