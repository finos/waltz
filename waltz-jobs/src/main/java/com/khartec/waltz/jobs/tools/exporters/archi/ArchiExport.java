package com.khartec.waltz.jobs.tools.exporters.archi;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.job.tools.exporters.archi.*;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.actor.ActorService;
import com.khartec.waltz.service.app_group.AppGroupService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXB;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.SetUtilities.minus;
import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.jobs.tools.exporters.archi.ArchiUtilities.*;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.application.ApplicationIdSelectionOptions.mkOpts;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class ArchiExport {

    private final AppGroupService appGroupService;
    private final ApplicationService applicationService;
    private final ActorService actorService;
    private final LogicalFlowService logicalFlowService;

    public static void main(String[] args) throws FileNotFoundException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ArchiExport exporter = ctx.getBean(ArchiExport.class);

        ModelType model = exporter.exportAppGroup(mkRef(EntityKind.APP_GROUP, 1L));

        ObjectFactory factory = new ObjectFactory();
        JAXB.marshal(factory.createModel(model), System.out);
        JAXB.marshal(factory.createModel(model), new FileOutputStream("c:\\temp\\waltz-archi-export.xml"));
    }


    @Autowired
    public ArchiExport(AppGroupService appGroupService,
                       ApplicationService applicationService,
                       ActorService actorService,
                       LogicalFlowService logicalFlowService) {
        this.appGroupService = appGroupService;
        this.applicationService = applicationService;
        this.actorService = actorService;
        this.logicalFlowService = logicalFlowService;
    }


    private ModelType exportAppGroup(EntityReference ref) {
        ModelType model = mkModel("waltz-export-example", "Example Waltz Archi Export");
        ApplicationIdSelectionOptions selectorOptions = mkOpts(ref);
        List<Application> apps = applicationService.findByAppIdSelector(selectorOptions);

        List<LogicalFlow> flows = logicalFlowService
                .findBySelector(selectorOptions)
                .stream()
                .filter(ArchiExport::isAppToAppFlow)
                .collect(Collectors.toList());

        Set<Tuple2<EntityReference, ApplicationComponent>> allApps = mkArchiApps(apps, flows);

        model.setElements(mkElements(model, allApps));

        Map<EntityReference, ? extends RealElementType> appComponentsByRef = indexArchiElements(allApps);

        List<Tuple2<EntityReference, Flow>> archiFlows = map(
                flows,
                f -> tuple(f.entityReference(), toArchi(f, appComponentsByRef)));
        RelationshipsType relationships = new RelationshipsType();
        relationships.getRelationship().addAll(map(archiFlows, t -> t.v2));
        model.setRelationships(relationships);

        return model;
    }

    private ElementsType mkElements(ModelType model, Set<Tuple2<EntityReference, ApplicationComponent>> allApps) {
        ElementsType elements = new ElementsType();
        List<ElementType> elementList = elements.getElement();
        elementList.addAll(map(allApps, t -> t.v2));
       return elements;
    }

    private Map<EntityReference, ? extends RealElementType> indexArchiElements(Set<Tuple2<EntityReference, ApplicationComponent>> allApps) {
        return MapUtilities.indexBy(
                    t -> t.v1,
                    t -> t.v2,
                    allApps);
    }


    private Set<Tuple2<EntityReference, ApplicationComponent>> mkArchiApps(List<Application> apps, List<LogicalFlow> flows) {
        Set<EntityReference> missingAppRefs = determineMissingApps(apps, flows);
        List<Tuple2<EntityReference, ApplicationComponent>>  directArchiApps = convertApps(apps);
        List<Tuple2<EntityReference, ApplicationComponent>>  indirectArchiApps = convertRefsToApps(missingAppRefs);

        return union(
                directArchiApps,
                indirectArchiApps);
    }


    private Set<EntityReference> determineMissingApps(List<Application> apps, List<LogicalFlow> flows) {
        Set<EntityReference> directAppRefs = SetUtilities.map(apps, Application::entityReference);
        Set<EntityReference> allEndpoints = flows.stream()
                .flatMap(f -> Stream.of(f.source(), f.target()))
                .collect(Collectors.toSet());

        return minus(allEndpoints, directAppRefs);
    }


    private List<Tuple2<EntityReference, ApplicationComponent>> convertApps(Collection<Application> apps) {
        return map(apps, app -> tuple(app.entityReference(), toArchi(app)));
    }


    private List<Tuple2<EntityReference, ApplicationComponent>> convertRefsToApps(Collection<EntityReference> refs) {
        return map(refs, ref -> tuple(ref, refToArchiApp(ref)));
    }


    private static boolean isAppToAppFlow(LogicalFlow f) {
        return isApp(f.source()) && isApp(f.target());
    }


    private static boolean isApp(EntityReference ref) {
        return ref.kind() == EntityKind.APPLICATION;
    }


}
