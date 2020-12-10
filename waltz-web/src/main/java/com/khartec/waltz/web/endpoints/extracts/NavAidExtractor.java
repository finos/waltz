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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.common.SvgUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.svg.SvgDiagram;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.svg.SvgDiagramService;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.StringUtilities.isNumericLong;
import static com.khartec.waltz.common.StringUtilities.toOptional;
import static com.khartec.waltz.model.EntityLinkUtilities.mkExternalIdLink;
import static com.khartec.waltz.model.EntityLinkUtilities.mkIdLink;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class NavAidExtractor extends BinaryDataBasedDataExtractor {

    @Value("${waltz.base.url:localhost}")
    private String baseUrl;

    private final MeasurableService measurableService;
    private final SvgDiagramService svgDiagramService;


    @Autowired
    public NavAidExtractor(MeasurableService measurableService,
                           SvgDiagramService svgDiagramService) {
        this.measurableService = measurableService;
        this.svgDiagramService = svgDiagramService;
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "nav-aid", ":svgDiagramId");

        get(path, (request, response) -> {
            Long diagramId = getLong(request,"svgDiagramId");

            SvgDiagram diagram = svgDiagramService.getById(diagramId);
            String svgWithLinks = addHyperLinks(diagram);

            String suggestedFilename = diagram.name()
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            return writeExtract(
                    suggestedFilename,
                    svgWithLinks.getBytes(),
                    request,
                    response);
        });
    }


    private String addHyperLinks(SvgDiagram diagram) {
        Function<String, Optional<String>> keyToUrl = mkKeyToUrl(diagram.group());

        return Unchecked.supplier(() -> SvgUtilities.addWaltzEntityLinks(
                                            diagram.svg(),
                                            diagram.keyProperty(),
                                            keyToUrl))
                .get();
    }


    private Function<String, Optional<String>> mkKeyToUrl(String groupId) {
        if (groupId.startsWith("NAVAID.MEASURABLE.")) {
            String categoryIdStr = groupId.replace("NAVAID.MEASURABLE.", "");
            if (isNumericLong(categoryIdStr)) {
                return mkMeasurableKeyToUrl(Long.parseLong(categoryIdStr));
            }
        } else {
            switch (groupId) {
                case "DATA_TYPE":
                    return mkDataTypeKeyToUrl();
                case "ORG_UNIT":
                    return mkOrgUnitKeyToUrl();
                case "ORG_TREE":
                    return mkPersonKeyToUrl();
            }
        }

        return (key) -> Optional.empty();
    }


    private Function<String, Optional<String>> mkMeasurableKeyToUrl(Long categoryId) {
        Map<String, Long> extToIdMap = measurableService.findExternalIdToIdMapByCategoryId(categoryId);
        return (extId) -> Optional.ofNullable(extToIdMap.get(extId))
                                    .map(id -> mkIdLink(baseUrl, EntityKind.MEASURABLE, id));
    }


    private Function<String, Optional<String>> mkDataTypeKeyToUrl() {
        return (dtCode) -> Optional.ofNullable(dtCode)
                                    .map(dc -> mkExternalIdLink(baseUrl, EntityKind.DATA_TYPE, dc));
    }


    private Function<String, Optional<String>> mkOrgUnitKeyToUrl() {
        return (unitId) -> toOptional(isNumericLong(unitId)
                                            ? mkIdLink(baseUrl, EntityKind.ORG_UNIT, Long.valueOf(unitId))
                                            : null);
    }


    private Function<String, Optional<String>> mkPersonKeyToUrl() {
        return (empId) -> Optional.ofNullable(empId)
                                    .map(eId -> mkExternalIdLink(baseUrl, EntityKind.PERSON, eId));
    }
}
