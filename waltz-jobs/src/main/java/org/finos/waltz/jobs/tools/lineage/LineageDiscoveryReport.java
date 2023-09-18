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

package org.finos.waltz.jobs.tools.lineage;

import org.finos.waltz.common.*;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.DIConfiguration;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.DOTExporter;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.IOUtilities.getFileResource;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;

public class LineageDiscoveryReport {

    private static List<String> staticHeaders = newArrayList(
            "Publisher",
            "Publisher Asset Code",
            "Consumer",
            "Consumer Asset Code",
            "Non-Strict Lineage",
            "Non-Strict Hops",
            "Strict Lineage",
            "Strict DataType",
            "Strict Hops",
            "Confidence"
    );

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDao logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        LogicalFlowDecoratorDao logicalFlowDecoratorDao = ctx.getBean(LogicalFlowDecoratorDao.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);

        // read publisher / consumer pairs from CSV
        Set<Tuple2<ExternalIdValue, ExternalIdValue>> sourceTargetCodes = parsePublisherConsumers("lineage/publishers_consumers.csv");
        if(sourceTargetCodes.isEmpty()) {
            System.out.println("No input data");
            return;
        }

        // Load reference data
        List<Application> allApplications = applicationDao.findAll();
        List<LogicalFlow> allActiveFlows = logicalFlowDao.findAllActive();
        List<DataTypeDecorator> allDecorators = logicalFlowDecoratorDao.findAll();

        List<RouteLookup> routeLookups = FindRoutes(logicalFlowDecoratorDao, allApplications, allActiveFlows, allDecorators, sourceTargetCodes);
        List<List<Object>> rows = prepareReportRows(routeLookups);

        byte[] excelReport = mkCSVReport(staticHeaders, rows);
        Files.write(Paths.get("output.csv"), excelReport);
        System.out.println("Completed processing");
    }


    private static List<List<Object>> prepareReportRows(List<RouteLookup> routeLookups) {

        List<List<Object>> collect = routeLookups
                .stream()
                .flatMap(row -> {
                    ArrayList<Object> reportRow = new ArrayList<>();

                    // publisher
                    reportRow.add(row.sourceApp().map(a -> a.name()).orElse("Not Found"));
                    reportRow.add(row.source().value());

                    // consumer
                    reportRow.add(row.targetApp().map(a -> a.name()).orElse("Not Found"));
                    reportRow.add(row.target().value());

                    // non-strict
                    Tuple2<Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>> nonStrict = row.nonStrictRoute().get();
                    reportRow.add(nonStrict != null && nonStrict.v2 != null ? true : false); // non-strict route
                    reportRow.add(nonStrict != null && nonStrict.v2 != null ? nonStrict.v2.getEdgeList().size() : "N/A"); // # non-strict hops


                    // strict
                    List<Tuple3<String, Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> strictRoutes = row.strictRoutes();


                    List<ArrayList<Object>> withStrict = strictRoutes.stream().map(t -> {
                        GraphPath<EntityReference, DataTypeEdge> sr = t.v3;
                        String dataType = t.v1;

                        ArrayList<Object> r = (ArrayList<Object>) reportRow.clone();
                        r.add(sr != null ? true : false); // strict route
                        r.add(dataType); // strict datatype
                        r.add(sr != null ? sr.getEdgeList().size() : "N/A"); // # strict hops
                        double confidence = calculateConfidence(sr, true);
                        r.add(confidence + "%"); // confidence

                        return r;
                    }).collect(toList());

                    if (withStrict.size() == 0) {
                        // no strict routes so these are blanks
                        reportRow.add(false); // strict route
                        reportRow.add("N/A"); // strict datatype
                        reportRow.add("N/A"); // # strict hops
                        double confidence = calculateConfidence(nonStrict.v2, false);
                        reportRow.add(confidence + "%"); // confidence
                        return Stream.of(reportRow);
                    } else {
                        return withStrict.stream();
                    }
                })
                .collect(toList());

        return collect;
    }


    private static byte[] mkCSVReport(List<String> headers,
                               List<List<Object>> reportRows) throws IOException {
        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(row)));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    /**
     * Naive confidence calculation
     * if non-strict start at 50% and knock 5% off for each hop
     * if strict start at 80%, minus 5% for each hop.  Average out extra routes
     *  - can later add support for authorised distributors
     * @param route
     * @param isStrict
     * @return
     */
    private static double calculateConfidence(GraphPath<EntityReference, DataTypeEdge> route,
                                              boolean isStrict) {
        if(route == null) {
            return 100d;
        }
        double startPercentage = isStrict ? 100d : 50d;
        int hopCount = route.getEdgeList().size();
        return startPercentage - hopCount * 5;
    }


    private static List<RouteLookup> FindRoutes(LogicalFlowDecoratorDao logicalFlowDecoratorDao,
                                                List<Application> allApplications,
                                                List<LogicalFlow> flows,
                                                List<DataTypeDecorator> dataTypeDecorators,
                                                Set<Tuple2<ExternalIdValue, ExternalIdValue>> sourceTargetCodes) {

        List<RouteLookup> routeLookups = new ArrayList<>(sourceTargetCodes.size());

        Map<String, Application> appsByAssetCode = indexBy(allApplications, a -> a.externalId().get());


        Graph<EntityReference, DataTypeEdge> graphAllDataTypes = createGraph(flows, dataTypeDecorators);

        // loop pairs - find each application
        // calculate shortest / all routes for a pair
        for (Tuple2<ExternalIdValue,ExternalIdValue> t: sourceTargetCodes) {
            ImmutableRouteLookup.Builder rlBuilder = ImmutableRouteLookup.builder()
                    .source(t.v1)
                    .target(t.v2);

            // fetch apps
            Application sourceApp = findAppByCode(appsByAssetCode, t.v1);
            Application targetApp = findAppByCode(appsByAssetCode, t.v2);

            rlBuilder
                    .sourceApp(ofNullable(sourceApp))
                    .targetApp(ofNullable(targetApp));


            System.out.printf(
                    "\n\nProcessing: %s to %s \n===================================\n",
                    t.v1,
                    t.v2);

            // ensure the applications exist
            if(sourceApp == null || targetApp == null) {
                if(sourceApp == null) {
                    System.out.printf("--- SourceApp not found from: %s\n", t.v1);
                    rlBuilder.addProcessingMessages(String.format("SourceApp not found from: %s", t.v1));
                }

                if(targetApp == null) {
                    System.out.printf("--- TargetApp not found from: %s\n", t.v2);
                    rlBuilder.addProcessingMessages(String.format("TargetApp not found from: %s", t.v2));
                }
                continue;
            }


            // check the applications have flows on the graphAllDataTypes
            if (!graphAllDataTypes.containsVertex(sourceApp.entityReference())) {
                System.out.printf("--- No flows defined for %s\n\n", sourceApp.entityReference());
                rlBuilder.addProcessingMessages(String.format("No flows defined for %s", sourceApp.entityReference()));
                continue;
            }

            if (!graphAllDataTypes.containsVertex(targetApp.entityReference())) {
                System.out.printf("--- No flows defined for %s\n\n", targetApp.entityReference());
                rlBuilder.addProcessingMessages(String.format("No flows defined for %s", targetApp.entityReference()));
                continue;
            }

            // find a route
            GraphPath<EntityReference, DataTypeEdge> nonStrictRoute = findRoute(graphAllDataTypes, sourceApp, targetApp);
            printRoute("Route irrespective of datatype", graphAllDataTypes, nonStrictRoute);
            rlBuilder.nonStrictRoute(ofNullable(Tuple.tuple(graphAllDataTypes, nonStrictRoute)));

            List<Tuple3<String, Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> strictRoutes = findStrictRoutes(logicalFlowDecoratorDao, flows, dataTypeDecorators, sourceApp, targetApp);
            rlBuilder.strictRoutes(strictRoutes);

            routeLookups.add(rlBuilder.build());
        }
        return routeLookups;
    }


    private static List<Tuple3<String, Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> findStrictRoutes(LogicalFlowDecoratorDao logicalFlowDecoratorDao,
                                                                                                                                         List<LogicalFlow> flows,
                                                                                                                                         List<DataTypeDecorator> dataTypeDecorators,
                                                                                                                                         Application sourceApp,
                                                                                                                                         Application targetApp) {
        // we have a route with a mix of data types
        // now construct a graph for each datatype from the source and target and find routes
        List<DataTypeDecorator> dataTypesForPair = getDecoratorsForSourceTarget(logicalFlowDecoratorDao, sourceApp, targetApp);
        Map<Long, Collection<DataTypeDecorator>> allDecoratorsByDataTypeId = MapUtilities.groupBy(dataTypeDecorators, DataTypeDecorator::dataTypeId);

        List<Long> dataTypeIds = dataTypesForPair.stream()
                .map(dtd -> dtd.decoratorEntity().id())
                .distinct()
                .collect(toList());

        List<Tuple3<String, Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> foundRoutes = new ArrayList<>();

        for (Long dataTypeId : dataTypeIds) {
            // fetch all the flows for this datatype
            Set<Long> dataFlowIdsForDt = allDecoratorsByDataTypeId.get(dataTypeId).stream()
                    .map(d -> d.dataFlowId())
                    .collect(toSet());
            List<LogicalFlow> flowsForDT = ListUtilities.filter(f -> dataFlowIdsForDt.contains(f.id().get()), flows);

            Graph<EntityReference, DataTypeEdge> dtGraph = createGraph(flowsForDT, (List<DataTypeDecorator>) allDecoratorsByDataTypeId.get(dataTypeId));
            GraphPath<EntityReference, DataTypeEdge> route = findRoute(
                    dtGraph,
                    sourceApp,
                    targetApp);

            if(route != null) {
                String dataTypeName = allDecoratorsByDataTypeId
                        .get(dataTypeId)
                        .stream()
                        .findFirst()
                        .get()
                        .decoratorEntity().name().get();

                foundRoutes.add(Tuple.tuple(dataTypeName, dtGraph, route));
                printRoute("Route for datatype: " + dataTypeName,
                            dtGraph,
                            route);
            }
        }

        return foundRoutes;
    }


    private static List<DataTypeDecorator> getDecoratorsForSourceTarget(LogicalFlowDecoratorDao logicalFlowDecoratorDao, Application sourceApp, Application targetApp) {
        Select<Record1<Long>> sourceTargetSelector = DSL.select(DSL.val(sourceApp.id().get()))
                .union(DSL.select(DSL.val(targetApp.id().get())));

        List<DataTypeDecorator> dataTypesForPair = logicalFlowDecoratorDao.findByAppIdSelector(sourceTargetSelector);
        return dataTypesForPair;
    }


    /**
     * Expect csv file with two columns
     * <source>,<target>
     * @param resourcePath
     * @return
     * @throws IOException
     */
    private static Set<Tuple2<ExternalIdValue, ExternalIdValue>> parsePublisherConsumers(String resourcePath) throws IOException {
        Resource resource = getFileResource(resourcePath);
        List<String> lines = IOUtilities.readLines(resource.getInputStream());
        Set<Tuple2<ExternalIdValue, ExternalIdValue>> tuples = lines
                .stream()
                .filter(StringUtilities::notEmpty)
                .skip(1) // skip the header
                .map(line -> line.split(","))
                .map(arr -> Tuple.tuple(
                        ExternalIdValue.of(safeTrim(arr[0])),
                        ExternalIdValue.of(safeTrim(arr[1]))))
                .collect(toSet());
        return tuples;
    }


    private static GraphPath<EntityReference, DataTypeEdge> findRoute(Graph<EntityReference, DataTypeEdge> graph,
                                                                      Application sourceApp,
                                                                      Application targetApp) {

        if(!graph.containsVertex(sourceApp.entityReference()) || !graph.containsVertex(targetApp.entityReference())) {
            //System.out.println("No route found\n\n");
            return null;
        }


        GraphPath<EntityReference, DataTypeEdge> route = findShortestPath(graph, sourceApp.entityReference(), targetApp.entityReference());
        if(route == null) {
            //Writer writer = generateDotGraph(graph);
            return null;
        }

        return route;
    }


    private static String routeToString(Graph<EntityReference, DataTypeEdge> graph,
                                        GraphPath<EntityReference, DataTypeEdge> route) {
        if(route == null) return null;

        // get apps along the edges
        List<DataTypeEdge> edgeList = route.getEdgeList();
        StringBuilder sb = new StringBuilder();

        // print out the route
        edgeList.forEach(edge -> {
            EntityReference source = graph.getEdgeSource(edge);
            EntityReference target = graph.getEdgeTarget(edge);
            String format = String.format(
                    "LF id: {%s} <%s (%s)> -- [%s] --> <%s (%s)> \n",
                    edge.getFlowId(),
                    source.name().get(),
                    source.externalId().orElse(""),
                    edge.getDataTypes(),
                    target.name().get(),
                    target.externalId().orElse(""));
            sb.append(format);
        });

        return sb.toString();
    }



    private static void printRoute(String header,
                                   Graph<EntityReference, DataTypeEdge> graph,
                                   GraphPath<EntityReference, DataTypeEdge> route) {
        if(route == null) return;

        System.out.println(header);
        System.out.println("-------------------------------");

        // get apps along the edges
        List<DataTypeEdge> edgeList = route.getEdgeList();

        // print out the route
        edgeList.forEach(edge -> {
            EntityReference source = graph.getEdgeSource(edge);
            EntityReference target = graph.getEdgeTarget(edge);
            System.out.printf(
                    "LF id: {%s} <%s (%s)> -- [%s] --> <%s (%s)> \n",
                    edge.getFlowId(),
                    source.name().get(),
                    source.externalId().orElse(""),
                    edge.getDataTypes(),
                    target.name().get(),
                    target.externalId().orElse(""));
        });
        System.out.println("-------------------------------");
    }


    private static Writer generateDotGraph(Graph<EntityReference, DataTypeEdge> graph) {
        DOTExporter<EntityReference, DataTypeEdge> exporter = new DOTExporter<>(
                v -> String.valueOf(v.id()),
                v -> v.name().get() + " (" + v.externalId().get() + ")",
                v -> v.getDataTypes() + " (" + v.getFlowId() + ")");
        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);
        return writer;
    }


    private static Application findAppByCode(Map<String, Application> appsByCode, ExternalIdValue assetCode) {
        Application app = appsByCode.get(assetCode.value());
        return app;
    }


    private static GraphPath<EntityReference, DataTypeEdge> findShortestPath(Graph<EntityReference, DataTypeEdge> g, EntityReference start, EntityReference end) {
        // Define a custom edge weight function to handle different edge labels
        DijkstraShortestPath<EntityReference, DataTypeEdge> dijkstraAlg =
                new DijkstraShortestPath<>(g);

//        SingleSourcePaths<EntityReference, DataTypeEdge> iPaths = dijkstraAlg.getPaths(start);
//        return iPaths.getPath(end);

        return dijkstraAlg.getPath(start, end);
    }


    private static Graph<EntityReference, DataTypeEdge> createGraph(List<LogicalFlow> flows, List<DataTypeDecorator> allDecorators) {

        Graph<EntityReference, DataTypeEdge> g = new DefaultDirectedGraph<>(DataTypeEdge.class);

        // add vertices
        flows
                .stream()
                .flatMap(f -> Stream.of(f.source(), f.target()))
                .distinct()
                .forEach(g::addVertex);

        Map<Long, Set<DataTypeDecorator>> dataTypesByFlowId = allDecorators.stream()
                .filter(d -> d.decoratorEntity().kind() == EntityKind.DATA_TYPE)
                .collect(Collectors.groupingBy(d -> d.dataFlowId(), Collectors.mapping(d -> d, toSet())));

        // add edges
        DataTypeDecorator unknownDecorator = ImmutableDataTypeDecorator.builder()
                .entityReference(EntityReference.mkRef(EntityKind.LOGICAL_DATA_FLOW, 0))
                .lastUpdatedBy("admin")
                .decoratorEntity(EntityReference.mkRef(EntityKind.DATA_TYPE, 0, "UNKNOWN"))
                .build();

        flows.forEach(f -> {
            List<String> dataTypeNames = dataTypesByFlowId
                    .getOrDefault(f.id().get(), SetUtilities.asSet(unknownDecorator))
                    .stream()
                    .map(d -> d.decoratorEntity().name().get())
                    .collect(toList());

            DataTypeEdge dataTypeEdge = new DataTypeEdge(f.id().get(), dataTypeNames);
            g.addEdge(f.source(),
                    f.target(),
                    dataTypeEdge);
        });

        return g;

    }


}
