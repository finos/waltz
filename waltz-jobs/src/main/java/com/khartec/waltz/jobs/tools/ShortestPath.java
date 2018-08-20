package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.DIConfiguration;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ShortestPath {

    private static String[] sourceAssetCodes = new String[] {
           "assetCode1",
           "assetCode2",
           "assetCode3"
    };


    private static String targetAssetCode = "26877-2";


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDao logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);
        List<LogicalFlow> allActive = logicalFlowDao.findAllActive();

        Graph<EntityReference, DefaultEdge> g = createGraph(allActive);

        Application targetApp = findFirstMatchByCode(applicationDao, targetAssetCode);
        Stream.of(sourceAssetCodes)
                .map(assetCode -> findFirstMatchByCode(applicationDao, assetCode))
                .filter(a -> a != null)
                .map(sourceApp -> {
                    System.out.printf("Route from: %s (%s)\n----------------------\n", sourceApp.name(), sourceApp.assetCode().orElse(""));
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
                    Map<Long, Application> appsById = MapUtilities.indexBy(a -> a.id().get(), applicationDao.findByIds(appIds));

                    edgeList.forEach(edge -> {
                        Application source = appsById.get(g.getEdgeSource(edge).id());
                        Application target = appsById.get(g.getEdgeTarget(edge).id());
                        System.out.printf(
                                "%s (%s) -> %s (%s) \n",
                                source.name(),
                                source.assetCode().orElse(""),
                                target.name(),
                                target.assetCode().orElse(""));
                    });

                    System.out.println();
                    System.out.println();

                });


    }

    private static Application findFirstMatchByCode(ApplicationDao applicationDao, String assetCode) {
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
                .forEach(v -> g.addVertex(v));

        flows
                .forEach(f -> g.addEdge(f.source(), f.target()));
        return g;
    }
}
