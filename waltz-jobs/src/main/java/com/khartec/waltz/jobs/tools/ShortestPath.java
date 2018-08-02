package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
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

import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toSet;

public class ShortestPath {


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDao logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);
        List<LogicalFlow> allActive = logicalFlowDao.findAllActive();


        Graph<EntityReference, DefaultEdge> g = createGraph(allActive);

        EntityReference start = mkRef(EntityKind.APPLICATION, 404);
        EntityReference end = mkRef(EntityKind.APPLICATION, 387);


        GraphPath<EntityReference, DefaultEdge> route = findShortestPath(g, start, end);

        List<DefaultEdge> edgeList = route.getEdgeList();


        Set<Long> appIds = edgeList.stream()
                .flatMap(e -> Stream.of(g.getEdgeSource(e).id(), g.getEdgeTarget(e).id()))
                .collect(toSet());

        Map<Long, Application> appsById = MapUtilities.indexBy(a -> a.id().get(), applicationDao.findByIds(appIds));

        edgeList.forEach(edge -> System.out.printf(
                    "%s -> %s\n",
                    appsById.get(g.getEdgeSource(edge).id()).name(),
                    appsById.get(g.getEdgeTarget(edge).id()).name()));

    }


    private static GraphPath<EntityReference, DefaultEdge> findShortestPath(Graph<EntityReference, DefaultEdge> g, EntityReference start, EntityReference end) {
        DijkstraShortestPath<EntityReference, DefaultEdge> dijkstraAlg =
                new DijkstraShortestPath<>(g);

        SingleSourcePaths<EntityReference, DefaultEdge> iPaths = dijkstraAlg.getPaths(start);
        return iPaths.getPath(end);
    }


    private static Graph<EntityReference, DefaultEdge> createGraph(List<LogicalFlow> allActive) {
        Graph<EntityReference, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        allActive
                .stream()
                .flatMap(f -> Stream.of(f.source(), f.target()))
                .distinct()
                .forEach(v -> g.addVertex(v));

        allActive
                .forEach(f -> g.addEdge(f.source(), f.target()));
        return g;
    }
}
