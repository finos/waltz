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

package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.application.ApplicationKind;
import org.finos.waltz.model.application.ApplicationsView;
import org.finos.waltz.model.application.ImmutableApplication;
import org.finos.waltz.model.application.ImmutableApplicationsView;
import org.finos.waltz.model.application.ImmutableAssessmentsView;
import org.finos.waltz.model.application.ImmutableMeasurableRatingsView;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.rating.RagRating;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApplicationViewExtractorTest {

    private static final long OU_ID = 100L;

    /**
     * Column ordering follows the static headers declared in the extractor:
     * Waltz Id, Name, Asset Code, Application Kind, Org Unit, Lifecycle Phase.
     */
    private static final int APPLICATION_KIND_COLUMN = 3;

    private final ApplicationViewExtractor extractor = new ApplicationViewExtractor(null, null, null);


    @Test
    void applicationKindColumnUsesApplicationKindDisplayName() {
        // regression for #7562: the export previously emitted the entity kind ("Application")
        // for every row rather than the application kind shown in the UI.
        assertEquals("Third Party", extractApplicationKindCell(ApplicationKind.THIRD_PARTY));
        assertEquals("In House", extractApplicationKindCell(ApplicationKind.IN_HOUSE));
        assertEquals("Customised", extractApplicationKindCell(ApplicationKind.CUSTOMISED));
    }


    @Test
    void applicationKindColumnDoesNotEmitEntityKind() {
        ImmutableApplication app = mkApp(ApplicationKind.THIRD_PARTY);
        Object cell = extractApplicationKindCell(ApplicationKind.THIRD_PARTY);
        assertNotEquals(app.kind().prettyName(), cell,
                "Application Kind column must not contain the entity kind (\"Application\")");
    }


    private Object extractApplicationKindCell(ApplicationKind kind) {
        List<List<Object>> rows = extractor.prepareReportRows(
                mkView(mkApp(kind)),
                Map.of(Optional.of(OU_ID), "Some Org Unit"));

        return rows.get(0).get(APPLICATION_KIND_COLUMN);
    }


    private static ApplicationsView mkView(ImmutableApplication app) {
        return ImmutableApplicationsView.builder()
                .addApplications(app)
                .primaryAssessments(ImmutableAssessmentsView.builder().build())
                .primaryRatings(ImmutableMeasurableRatingsView.builder().build())
                .build();
    }


    private static ImmutableApplication mkApp(ApplicationKind kind) {
        return ImmutableApplication.builder()
                .id(1L)
                .name("Test App")
                .description("A test application")
                .assetCode(ExternalIdValue.of("ASSET-1"))
                .organisationalUnitId(OU_ID)
                .applicationKind(kind)
                .lifecyclePhase(LifecyclePhase.PRODUCTION)
                .overallRating(RagRating.G)
                .entityLifecycleStatus(EntityLifecycleStatus.ACTIVE)
                .isRemoved(false)
                .build();
    }
}
