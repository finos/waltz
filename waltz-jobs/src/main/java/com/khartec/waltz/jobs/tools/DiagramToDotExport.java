/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.flow_diagram.FlowDiagramEntityService;
import com.khartec.waltz.service.flow_diagram.FlowDiagramService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.model.EntityReference.mkRef;

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
        StringBuilder sb = new StringBuilder()
            .append("digraph G {")
            .append(renderApplications(apps))
            .append(renderFlows(flows, appsById))
            .append("}");

        System.out.println(sb);
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
                .map(a -> quote(a.assetCode())
                        + "["
                        + attr("label", a.name())
                        + ", "
                        + attr("nar", a.assetCode())
                        + "]")
                .reduce("", (acc, s) -> acc + "\n\t" + s);
    }

    private static String attr(String name, Object value) {
        return name + "=" + quote(value == null ? "" : value.toString());
    }


    private static String quote(Optional<String> str) {
        return str.map(s -> quote(s)).orElse("");
    }

    private static String quote(String str) {
        return "\"" + str + "\"";
    }

    private static String renderFlows(List<LogicalFlow> flows,
                                      Map<Long, Application> appsById) {
        return flows.stream()
                .map(f ->
                        quote(appsById.get(f.source().id()).assetCode())
                        + " -> "
                        + quote(appsById.get(f.target().id()).assetCode()))
                .reduce("", (acc, s) -> acc + "\n\t" + s);
    }

}
