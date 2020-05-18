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

package com.khartec.waltz.model.entity_relationship;

import com.khartec.waltz.model.EntityKind;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collections;
import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.fromArray;
import static com.khartec.waltz.model.EntityKind.*;
import static org.jooq.lambda.tuple.Tuple.tuple;


public enum RelationshipKind {

    HAS(Collections.emptySet()),

    DEPRECATES(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION),
            tuple(CHANGE_INITIATIVE, MEASURABLE)
    )),

    PARTICIPATES_IN(fromArray(
            tuple(APPLICATION, CHANGE_INITIATIVE)
    )),

    RELATES_TO(fromArray(
            tuple(APP_GROUP, APP_GROUP),
            tuple(APP_GROUP, CHANGE_INITIATIVE),
            tuple(APP_GROUP, MEASURABLE),
            tuple(APP_GROUP, ROADMAP),
            tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE),
            tuple(CHANGE_INITIATIVE, MEASURABLE),
            tuple(CHANGE_INITIATIVE, DATA_TYPE),
            tuple(CHANGE_INITIATIVE, APP_GROUP),
            tuple(MEASURABLE, APP_GROUP),
            tuple(MEASURABLE, MEASURABLE),
            tuple(MEASURABLE, CHANGE_INITIATIVE),
            tuple(ORG_UNIT, ROADMAP),
            tuple(LICENCE, APPLICATION)
    )),

    SUPPORTS(fromArray(
            tuple(APPLICATION, CHANGE_INITIATIVE),
            tuple(ACTOR, CHANGE_INITIATIVE)
    )),

    APPLICATION_NEW(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    )),

    APPLICATION_FUNCTIONAL_CHANGE(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    )),

    APPLICATION_DECOMMISSIONED(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    )),

    APPLICATION_NFR_CHANGE(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    )),

    DATA_PUBLISHER(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    )),

    DATA_CONSUMER(fromArray(
            tuple(CHANGE_INITIATIVE, APPLICATION)
    ));


    private Set<Tuple2<EntityKind, EntityKind>> allowedEntityKinds;


    RelationshipKind(Set<Tuple2<EntityKind, EntityKind>> allowedEntityKinds) {
        this.allowedEntityKinds = allowedEntityKinds;
    }


    public Set<Tuple2<EntityKind, EntityKind>> getAllowedEntityKinds() {
        return this.allowedEntityKinds;
    }

}
