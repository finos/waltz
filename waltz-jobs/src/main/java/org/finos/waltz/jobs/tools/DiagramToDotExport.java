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

package org.finos.waltz.jobs.tools;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.flow_diagram.FlowDiagramEntityService;
import org.finos.waltz.service.flow_diagram.FlowDiagramService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.model.EntityReference.mkRef;

/**
 * Created by watkdav on 06/07/2018.
 */
public class DiagramToDotExport {


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        FlowDiagramService flowDiagramService = ctx.getBean(FlowDiagramService.class);
        FlowDiagramEntityService flowDiagramEntityService = ctx.getBean(FlowDiagramEntityService.class);

        ApplicationService applicationService = ctx.getBean(ApplicationService.class);
        LogicalFlowService logicalFlowService = ctx.getBean(LogicalFlowService.class);

        EntityReference diagRef = mkRef(EntityKind.FLOW_DIAGRAM, 1L);

        IdSelectionOptions options = IdSelectionOptions.mkOpts(diagRef, HierarchyQueryScope.EXACT);
        List<Application> apps = applicationService.findByAppIdSelector(options);
        List<LogicalFlow> flows = logicalFlowService.findBySelector(options);

        Map<Long, Application> appsById = indexBy(a -> a.id().get(), apps);

        System.out.println("------");
        String digraph = String.format(
                "digraph G { %s %s}",
                renderApplications(apps),
                renderFlows(flows, appsById));

        System.out.println(digraph);
        System.out.println("-----");
        /*
        digraph G {
            "Welcome" -> "To"
            "To" -> "Web"
            "To" -> "GraphViz!"
        }
        */

    }

    private static String renderApplications(List<Application> apps) {
        return apps.stream()
                .map(a -> quoteExtId(a.assetCode())
                        + "["
                        + attr("label", a.name())
                        + ", "
                        + attr("nar", a.assetCode())
                        + "]")
                .reduce("", (acc, s) -> acc + "\n\t" + s);
    }

    private static String attr(String name, Object value) {
        return name + "=" + quoteStr(value == null ? "" : value.toString());
    }


    private static String quoteExtId(Optional<ExternalIdValue> str) {
        return str.map(s -> quoteStr(s.value())).orElse("");
    }

    private static String quoteStr(String str) {
        return "\"" + str + "\"";
    }

    private static String renderFlows(List<LogicalFlow> flows,
                                      Map<Long, Application> appsById) {
        return flows.stream()
                .map(f ->
                        quoteExtId(appsById.get(f.source().id()).assetCode())
                        + " -> "
                        + quoteExtId(appsById.get(f.target().id()).assetCode()))
                .reduce("", (acc, s) -> acc + "\n\t" + s);
    }

}
