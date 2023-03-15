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

package org.finos.waltz.service.assessment_definition;


import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.legal_entity.LegalEntityRelationshipDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.user.UserPreferenceDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.user.UserPreference;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.common.StringUtilities.splitThenMap;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.user.UserPreference.mkPref;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class AssessmentDefinitionService {

    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final MeasurableDao measurableDao;
    private final LegalEntityRelationshipDao legalEntityRelationshipDao;
    private final UserPreferenceDao userPreferenceDao;


    @Autowired
    public AssessmentDefinitionService(AssessmentDefinitionDao assessmentDefinitionDao,
                                       MeasurableDao measurableDao,
                                       LegalEntityRelationshipDao legalEntityRelationshipDao,
                                       UserPreferenceDao userPreferenceDao) {

        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(userPreferenceDao, "userPreferenceDao cannot be null");
        checkNotNull(legalEntityRelationshipDao, "legalEntityRelationshipDao cannot be null");

        this.measurableDao = measurableDao;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.legalEntityRelationshipDao = legalEntityRelationshipDao;
        this.userPreferenceDao = userPreferenceDao;
    }


    public AssessmentDefinition getById(long id) {
        return assessmentDefinitionDao.getById(id);
    }


    public Set<AssessmentDefinition> findAll() {
        return assessmentDefinitionDao.findAll();
    }


    public Set<AssessmentDefinition> findByEntityKind(EntityKind kind) {
        return assessmentDefinitionDao.findByEntityKind(kind);
    }


    public Set<AssessmentDefinition> findByEntityKindAndQualifier(EntityKind kind, EntityReference qualifierReference) {
        return assessmentDefinitionDao.findByEntityKindAndQualifier(kind, qualifierReference);
    }

    public Set<AssessmentDefinition> findByEntityReference(EntityReference entityReference) {
        switch (entityReference.kind()) {
            case MEASURABLE:
                Measurable m = measurableDao.getById(entityReference.id());
                return assessmentDefinitionDao.findByEntityKindAndQualifier(
                        entityReference.kind(),
                        mkRef(EntityKind.MEASURABLE_CATEGORY, m.categoryId()));
            default:
                return findByEntityKind(entityReference.kind());
        }
    }


    public Long save(AssessmentDefinition def) {
        return assessmentDefinitionDao.save(def);
    }


    public boolean remove(long definitionId) {
        return assessmentDefinitionDao.remove(definitionId) == 1;
    }


    public Set<AssessmentDefinition> findFavouritesForUser(String username) {
        Tuple2<Set<Long>, Set<Long>> t = loadFavouriteIncludedAndExcludedIds(username);
        return assessmentDefinitionDao.findFavourites(t.v1, t.v2);
    }


    public Set<AssessmentDefinition> addFavourite(long defnId,
                                                  String username) {

        Tuple2<Set<Long>, Set<Long>> includedAndExcludedIds = loadFavouriteIncludedAndExcludedIds(username);

        Set<Long> newIncluded = SetUtilities.add(includedAndExcludedIds.v1, defnId);
        Set<Long> newExcluded = SetUtilities.remove(includedAndExcludedIds.v2, defnId);

        storeFavourites(username, newIncluded, newExcluded);
        return findFavouritesForUser(username);
    }


    public Set<AssessmentDefinition> removeFavourite(long defnId,
                                                  String username) {

        Tuple2<Set<Long>, Set<Long>> includedAndExcludedIds = loadFavouriteIncludedAndExcludedIds(username);

        Set<Long> newIncluded = SetUtilities.remove(includedAndExcludedIds.v1, defnId);
        Set<Long> newExcluded = SetUtilities.add(includedAndExcludedIds.v2, defnId);

        storeFavourites(username, newIncluded, newExcluded);
        return findFavouritesForUser(username);
    }


    private void storeFavourites(String username, Set<Long> newIncluded, Set<Long> newExcluded) {
        userPreferenceDao.savePreferencesForUser(
            username,
            asList(
                mkPref(
                    "assessment-rating.favourites.included",
                    join(newIncluded, ",")),
                mkPref(
                    "assessment-rating.favourites.excluded",
                    join(newExcluded, ","))));
    }


    private Tuple2<Set<Long>, Set<Long>> loadFavouriteIncludedAndExcludedIds(String username) {
        Map<String, UserPreference> preferencesForUser = indexBy(
                userPreferenceDao.getPreferencesForUser(username),
                UserPreference::key);

        Set<Long> included = toFavouriteIds(preferencesForUser.get("assessment-rating.favourites.included"));
        Set<Long> explicitlyExcluded = toFavouriteIds(preferencesForUser.get("assessment-rating.favourites.excluded"));

        return tuple(included, explicitlyExcluded);
    }


    private Set<Long> toFavouriteIds(UserPreference pref) {
        if (pref == null) {
            return Collections.emptySet();
        } else {
            return SetUtilities.fromCollection(splitThenMap(
                    pref.value(),
                    ",",
                    Long::parseLong));
        }
    }

}
