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

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.DIConfiguration;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ShortestPath {

    private static ExternalIdValue[] sourceAssetCodes = new ExternalIdValue[] {
            ExternalIdValue.of("26716-1")
    };


    private static final ExternalIdValue targetAssetCode = ExternalIdValue.of("167106-1");


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDao logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);
        List<LogicalFlow> allActive = logicalFlowDao.findAllActive();

        Graph<EntityReference, DefaultEdge> g = createGraph(allActive);

        Application targetApp = findFirstMatchByCode(applicationDao, targetAssetCode);
        Stream.of(sourceAssetCodes)
                .map(assetCode -> findFirstMatchByCode(applicationDao, assetCode))
                .filter(Objects::nonNull)
                .map(sourceApp -> {
                    System.out.printf(
                            "Route from: %s (%s)\n----------------------\n",
                            sourceApp.name(),
                            ExternalIdValue.orElse(sourceApp.assetCode(), ""));
                    return sourceApp.entityReference();
                })
                .filter(sourceRef -> {
                    if (!g.containsVertex(sourceRef)) {
                        System.out.println("No flows defined for application\n\n");
                        return false;
                    }
                    return true;
                })
                .map(sourceRef -> findShortestPath(g, sourceRef, targetApp.entityReference()))
                .filter(route -> {
                    if (route == null) {
                        System.out.println("No route found\n\n");
                        return false;
                    }
                    return true;
                })
                .forEach(route -> {
                    List<DefaultEdge> edgeList = route.getEdgeList();
                    Set<Long> appIds = edgeList.stream()
                            .flatMap(e -> Stream.of(g.getEdgeSource(e).id(), g.getEdgeTarget(e).id()))
                            .collect(toSet());
                    Map<Long, Application> appsById = MapUtilities.indexBy(
                            a -> a.id().get(),
                            applicationDao.findByIds(appIds));

                    edgeList.forEach(edge -> {
                        Application source = appsById.get(g.getEdgeSource(edge).id());
                        Application target = appsById.get(g.getEdgeTarget(edge).id());
                        System.out.printf(
                                "%s (%s) -> %s (%s) \n",
                                source.name(),
                                ExternalIdValue.orElse(source.assetCode(), ""),
                                target.name(),
                                ExternalIdValue.orElse(target.assetCode(), ""));
                    });

                    System.out.println();
                    System.out.println();

                });
    }


    private static Application findFirstMatchByCode(ApplicationDao applicationDao, ExternalIdValue assetCode) {
        List<Application> apps = applicationDao.findByAssetCode(assetCode);
        // should be only one
        return apps.size() > 0
            ? apps.get(0)
            : null;
    }


    private static GraphPath<EntityReference, DefaultEdge> findShortestPath(Graph<EntityReference, DefaultEdge> g, EntityReference start, EntityReference end) {
        DijkstraShortestPath<EntityReference, DefaultEdge> dijkstraAlg =
                new DijkstraShortestPath<>(g);

        SingleSourcePaths<EntityReference, DefaultEdge> iPaths = dijkstraAlg.getPaths(start);
        return iPaths.getPath(end);
    }


    private static Graph<EntityReference, DefaultEdge> createGraph(List<LogicalFlow> flows) {
        Graph<EntityReference, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        flows
                .stream()
                .flatMap(f -> Stream.of(f.source(), f.target()))
                .distinct()
                .forEach(g::addVertex);

        flows
                .forEach(f -> g.addEdge(f.source(), f.target()));
        return g;
    }
}
