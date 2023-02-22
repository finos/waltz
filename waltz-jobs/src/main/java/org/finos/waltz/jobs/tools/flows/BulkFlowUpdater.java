package org.finos.waltz.jobs.tools.flows;

import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkFlowUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(BulkFlowUpdater.class);

    private final LogicalFlowService logicalFlowService;
    private final DataTypeDecoratorService dataTypeDecoratorService;
    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;

    private String username = "steven.jackson@db.com";

    @Autowired
    public BulkFlowUpdater(LogicalFlowService logicalFlowService,
                           DataTypeDecoratorService dataTypeDecoratorService,
                           PhysicalSpecDataTypeService physicalSpecDataTypeService) {
        this.logicalFlowService = logicalFlowService;
        this.dataTypeDecoratorService = dataTypeDecoratorService;
        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
    }


    private void go() {
        LOG.info("Start import");
        LOG.info("About to process {} commands", ExampleFlowUpdateCommands.commands.size());
        Map<FlowUpdateCommandType, Collection<FlowUpdateCommand>> commandsByAction = groupBy(
                ExampleFlowUpdateCommands.commands,
                FlowUpdateCommand::action);

        processAdditions(commandsByAction.get(FlowUpdateCommandType.ADD));
        processRemovals(commandsByAction.get(FlowUpdateCommandType.REMOVE));

        LOG.info("Rippling data types from physicals back to logicals");
        physicalSpecDataTypeService.rippleDataTypesToLogicalFlows();
        LOG.info("End import");
    }


    private void processRemovals(Collection<FlowUpdateCommand> commands) {
        LOG.info("Processing {} removals", commands.size());
        Map<Long, Collection<EntityReference>> typesToRemoveByFlowId = groupBy(
                commands,
                FlowUpdateCommand::logicalFlowId,
                FlowUpdateCommand::dataTypeRef);

        Map<Long, Tuple2<EntityReference, EntityReference>> flowToSourceTargetMap = indexBy(
                commands,
                FlowUpdateCommand::logicalFlowId,
                c -> tuple(c.sourceEntityRef(), c.targetEntityRef()));

        typesToRemoveByFlowId
                .entrySet()
                .stream()
                .forEach(kv -> {
                    Long lfId = kv.getKey();
                    Tuple2<EntityReference, EntityReference> sourceAndTarget = flowToSourceTargetMap.get(lfId);
                    Collection<EntityReference> dts = kv.getValue();
                    LOG.debug(
                            "Updating datatypes for logical flow: http://waltz.intranet.db.com/waltz/logical-flow/{}?sections=7, ({} -> {}) removing dataTypes: {}",
                            lfId,
                            sourceAndTarget.v1.name().orElse("?"),
                            sourceAndTarget.v2.name().orElse("?"),
                            map(dts, dt -> dt.name().orElse("?")));

                    dataTypeDecoratorService.updateDecorators(
                            username,
                            mkRef(EntityKind.LOGICAL_DATA_FLOW, lfId),
                            emptySet(),
                            map(dts, EntityReference::id));
                });

        LOG.info("Completed removal of flow data types");
    }


    private void processAdditions(Collection<FlowUpdateCommand> commands) {
        LOG.info("Processing {} additions", commands.size());
        Map<Operation, Collection<FlowUpdateCommand>> byOp = groupBy(commands, c -> c.logicalFlowId() == null ? Operation.ADD : Operation.UPDATE);
        processNewFlows(byOp.get(Operation.ADD));
        processUpdatedFlows(byOp.get(Operation.UPDATE));
        LOG.info("Completed processing of additions");
    }


    private void processNewFlows(Collection<FlowUpdateCommand> commands) {
        LOG.info("Processing {} new flows", commands.size());
        Set<AddLogicalFlowCommand> addCmds = commands
                .stream()
                .map(c -> ImmutableAddLogicalFlowCommand
                        .builder()
                        .source(c.sourceEntityRef())
                        .target(c.targetEntityRef())
                        .build())
                .collect(toSet());

        LOG.info("Adding {} flows", addCmds.size());
        addCmds.forEach(c -> LOG.debug(
                "Adding flow between {} and {},  http://waltz.intranet.db.com/waltz/{}/{}?sections=9;7",
                c.source().name().orElse("?"),
                c.target().name().orElse("?"),
                c.source().kind() == EntityKind.ACTOR ? "actor" : "application",
                c.source().id()));

        Set<LogicalFlow> logicalFlows = logicalFlowService.addFlows(
                addCmds,
                username);

        LOG.debug("{} Flows created, modifying the commands to update types", logicalFlows.size());
        Map<Tuple2<EntityReference, EntityReference>, Long> srcAndTargetToFlowIdMap = indexBy(
                logicalFlows,
                l -> tuple(l.source(), l.target()),
                l -> l.id().get());

        Set<FlowUpdateCommand> toUpdate = commands
                .stream()
                .map(c -> ImmutableFlowUpdateCommand
                        .copyOf(c)
                        .withLogicalFlowId(srcAndTargetToFlowIdMap.get(tuple(
                                c.sourceEntityRef(),
                                c.targetEntityRef()))))
                .filter(c -> c.logicalFlowId() != null)
                .collect(toSet());

        LOG.debug("Sending the flows to be updated");
        processUpdatedFlows(toUpdate);
        LOG.info("Completed processing new flows");
    }


    private void processUpdatedFlows(Collection<FlowUpdateCommand> commands) {
        LOG.info("Processing {} updated flows", commands.size());
        Map<Long, Collection<EntityReference>> typesToAddByFlowId = groupBy(
                commands,
                FlowUpdateCommand::logicalFlowId,
                FlowUpdateCommand::dataTypeRef);

        Map<Long, Tuple2<EntityReference, EntityReference>> flowToSourceTargetMap = indexBy(
                commands,
                FlowUpdateCommand::logicalFlowId,
                c -> tuple(c.sourceEntityRef(), c.targetEntityRef()));

        typesToAddByFlowId
                .entrySet()
                .stream()
                .forEach(kv -> {
                    Long lfId = kv.getKey();
                    Tuple2<EntityReference, EntityReference> sourceAndTarget = flowToSourceTargetMap.get(lfId);
                    Collection<EntityReference> dts = kv.getValue();
                    LOG.debug(
                            "Updating datatypes for logical flow: http://waltz.intranet.db.com/waltz/logical-flow/{}?sections=7, ({} -> {}) adding dataTypes: {}",
                            lfId,
                            sourceAndTarget.v1.name().orElse("?"),
                            sourceAndTarget.v2.name().orElse("?"),
                            map(dts, dt -> dt.name().orElse("?")));

                    dataTypeDecoratorService.updateDecorators(
                            username,
                            mkRef(EntityKind.LOGICAL_DATA_FLOW, lfId),
                            map(dts, EntityReference::id),
                            emptySet());
                });

        LOG.info("Completed addition of flow data types");
    }


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ctx.getBean(BulkFlowUpdater.class).go();
    }

}
