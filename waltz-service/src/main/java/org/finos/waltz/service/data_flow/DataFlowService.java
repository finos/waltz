package org.finos.waltz.service.data_flow;

import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowDeleteCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.physical_flow.PhysicalFlowDeleteCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowDeleteCommandResponse;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import org.finos.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import org.finos.waltz.model.proposed_flow.DeletePhysicalFlowResponse;
import org.finos.waltz.model.proposed_flow.ImmutableDeletePhysicalFlowResponse;
import org.finos.waltz.model.proposed_flow.ImmutableLogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.difference;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static org.finos.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class DataFlowService {
    private static final Logger LOG = LoggerFactory.getLogger(DataFlowService.class);

    private final ProposedFlowDao proposedFlowDao;
    public final LogicalFlowService logicalFlowService;
    public final PhysicalFlowService physicalFlowService;
    public final EntityWorkflowService entityWorkflowService;
    private final PhysicalSpecificationService specificationService;
    private final DataTypeDecoratorService dataTypeDecoratorService;

    @Autowired
    public DataFlowService(ProposedFlowDao proposedFlowDao, LogicalFlowService logicalFlowService, PhysicalFlowService physicalFlowService,
                           EntityWorkflowService entityWorkflowService, PhysicalSpecificationService specificationService,
                           DataTypeDecoratorService dataTypeDecoratorService) {
        this.proposedFlowDao = proposedFlowDao;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowService = physicalFlowService;
        this.entityWorkflowService = entityWorkflowService;
        this.specificationService = specificationService;
        this.dataTypeDecoratorService = dataTypeDecoratorService;
    }

    /**
     * Creates logical and physical flows from the ProposedFlow.
     *
     * @param proposedFlowId primary key of the ProposedFlow
     * @param username actor requesting the creation
     * @return immutable response containing the created flows
     * @throws FlowCreationException if either creation step fails
     */
    public LogicalPhysicalFlowCreationResponse createLogicalAndPhysicalFlowFromProposedFlowDef(long proposedFlowId, String username) throws FlowCreationException {

        Objects.requireNonNull(username, "username must not be null");
        PhysicalFlowCreateCommandResponse physicalFlowCreateCommandResponse;
        LogicalFlow logicalFlow;

        ProposedFlowResponse proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlowId);
        LOG.info("Proposed flow definition : {}", proposedFlow);

        Optional<Long> logicalFlowId = proposedFlow.flowDef().logicalFlowId();

        if (logicalFlowId.isPresent()) {
            LOG.debug("Logical flow already exists, logical flow id is : {}, so skipping creation", logicalFlowId.get());
            logicalFlow = logicalFlowService.getById(logicalFlowId.get());
        } else {
            //create logical flow
            logicalFlow = createLogicalFlow(proposedFlow, username);
            saveEntityWorkflowResult(proposedFlow, logicalFlow.entityReference(), username);
        }

        //create physical flow
        physicalFlowCreateCommandResponse = createPhysicalFlow(proposedFlow, username, logicalFlow.id());
        if (!proposedFlow.flowDef().specification().id().isPresent()) {
            saveEntityWorkflowResult(proposedFlow, mkRef(PHYSICAL_SPECIFICATION, physicalFlowCreateCommandResponse.specificationId()), username);
        }
        saveEntityWorkflowResult(proposedFlow, physicalFlowCreateCommandResponse.entityReference(), username);

        LOG.info("Successfully created flows for proposedFlowId = {}", proposedFlowId);

        return ImmutableLogicalPhysicalFlowCreationResponse.builder()
                .logicalFlow(logicalFlow)
                .physicalFlowCreateCommandResponse(physicalFlowCreateCommandResponse)
                .build();
    }

    private void saveEntityWorkflowResult(ProposedFlowResponse proposedFlow, EntityReference resultingEntity, String username) {
        entityWorkflowService.createEntityWorkflowResult(
                proposedFlow.workflowState().workflowId(),
                proposedFlow.workflowState().entityReference(),
                resultingEntity,
                username);
    }

    public AddLogicalFlowCommand mapProposedFlowToAddLogicalFlowCommand(ProposedFlowResponse proposedFlow) {
        return ImmutableAddLogicalFlowCommand.builder()
                .source(proposedFlow.flowDef().source())
                .target(proposedFlow.flowDef().target())
                .build();
    }

    public PhysicalFlowCreateCommand mapProposedFlowToPhysicalFlowCreateCommand(ProposedFlowResponse proposedFlow, Optional<Long> logicalFlowId) {
        return ImmutablePhysicalFlowCreateCommand.builder()
                .specification(proposedFlow.flowDef().specification())
                .logicalFlowId(proposedFlow.flowDef().logicalFlowId().orElse(logicalFlowId.get()))
                .flowAttributes(proposedFlow.flowDef().flowAttributes())
                .dataTypeIds(proposedFlow.flowDef().dataTypeIds())
                .build();
    }

    private PhysicalFlowCreateCommand mapProposedFlowCommandToPhysicalFlowCreateCommand(ProposedFlowCommand command) {
        return ImmutablePhysicalFlowCreateCommand.builder()
                .specification(command.specification())
                .logicalFlowId(command.logicalFlowId().get())
                .flowAttributes(command.flowAttributes())
                .dataTypeIds(command.dataTypeIds())
                .build();
    }


    public LogicalFlow createLogicalFlow(ProposedFlowResponse proposedFlow, String username) throws FlowCreationException {
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlow);

        LOG.info("User: {}, adding new logical flow: {}", username, addCmd);
        try {
            return logicalFlowService.addFlow(addCmd, username);
        } catch (Exception ex) {
            LOG.error("Failed to create logical flow from proposedFlowId={}", proposedFlow.id(), ex);
            throw new FlowCreationException("Logical flow creation failed", ex);
        }
    }

    public PhysicalFlowCreateCommandResponse createPhysicalFlow(ProposedFlowResponse proposedFlow, String username, Optional<Long> logicalFlowId) throws FlowCreationException {
        PhysicalFlowCreateCommand command = mapProposedFlowToPhysicalFlowCreateCommand(proposedFlow, logicalFlowId);

        LOG.info("User: {}, adding new physical flow: {}", username, command);
        try {
            return physicalFlowService.create(command, username);
        } catch (Exception ex) {
            LOG.error("Failed to create physical flow from proposedFlowId={}", proposedFlow.id(), ex);
            throw new FlowCreationException("Physical flow creation failed", ex);
        }
    }

    public boolean editPhysicalFlow(ProposedFlowResponse proposedFlow, String username) {
        checkNotNull(proposedFlow.flowDef().physicalFlowId().get(), "physical flow id can not be null");
        checkNotEmpty(proposedFlow.flowDef().dataTypeIds(), "dataTypeIds can not be empty");

        PhysicalFlow physicalFlow = physicalFlowService.getByIdAndIsRemoved(proposedFlow.flowDef().physicalFlowId().get(), false);
        checkNotNull(physicalFlow, "physical flow can not be null");

        //fetch data type id's from DB and request
        Set<Long> dataTypeIdsInDB = specificationService.getDataTypesByPhysicalFlowId(physicalFlow.id().get());
        Set<Long> dataTypeIdsInRequest = new HashSet<>(proposedFlow.flowDef().dataTypeIds());

        //Determine which id's to add and remove
        Set<Long> toAdd = difference(dataTypeIdsInRequest, dataTypeIdsInDB);
        Set<Long> toRemove = difference(dataTypeIdsInDB, dataTypeIdsInRequest);

        return dataTypeDecoratorService.updateDecorators(
                username,
                mkRef(EntityKind.PHYSICAL_SPECIFICATION, physicalFlow.specificationId()),
                toAdd,
                toRemove);
    }


    public Long getPhysicalFlowIfExist(ProposedFlowCommand command, String username) {
        return physicalFlowService.getPhysicalFlowIfExist(mapProposedFlowCommandToPhysicalFlowCreateCommand(command),
                username);
    }

    /**
     * Soft-deletes a physical flow and – when safe – its specification and the
     * associated logical flow together with its data-type decorators.
     *
     * @param physicalFlowId id of the flow to delete
     * @param username user performing the operation
     * @return immutable response object with the ids of the deleted artefacts
     */
    public DeletePhysicalFlowResponse deletePhysicalFlow(Long physicalFlowId, String username) {

        checkNotNull(physicalFlowId, "physicalFlowId must not be null");
        checkNotNull(username, "username must not be null");

        PhysicalFlow physicalFlow = physicalFlowService.getById(physicalFlowId);
        checkNotNull(physicalFlow, "No physical flow found");

        LOG.info("[deletePhysicalFlow] user={} physicalFlowId={}", username, physicalFlowId);

        PhysicalFlowDeleteCommand physicalFlowDeleteCommand = buildPhysicalFlowDeleteCommand(physicalFlowId);

        //soft delete physical flow
        PhysicalFlowDeleteCommandResponse physicalFlowDeleteCommandResponse = physicalFlowService.delete(physicalFlowDeleteCommand, username);

        //check specification is unused
        if (physicalFlowDeleteCommandResponse.isSpecificationUnused()) {
            PhysicalSpecificationDeleteCommand physicalSpecDeleteCmd = buildPhysicalSpecificationDeleteCommand(physicalFlow.specificationId());
            //soft delete physical specification
            specificationService.markRemovedIfUnused(physicalSpecDeleteCmd, username);
        }

        //check last physical flow
        if (physicalFlowDeleteCommandResponse.isLastPhysicalFlow()) {
            LOG.info("[deletePhysicalFlow] last physical flow removed – deleting logical flow {}", physicalFlow.logicalFlowId());

            //soft delete associated logical flow
            logicalFlowService.removeFlow(physicalFlow.logicalFlowId(), username);

            //delete logical flow decorator
            deleteLogicalFlowDecorator(username, physicalFlow.logicalFlowId());
        }

        LOG.info("[deletePhysicalFlow] completed successfully for physicalFlowId={}", physicalFlowId);

        return ImmutableDeletePhysicalFlowResponse.builder()
                .logicalFlowId(physicalFlow.logicalFlowId())
                .physicalFlowId(physicalFlowId)
                .specificationId(physicalFlow.specificationId())
                .build();
    }

    private PhysicalFlowDeleteCommand buildPhysicalFlowDeleteCommand(Long physicalFlowId) {
        return ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(physicalFlowId)
                .build();
    }

    private PhysicalSpecificationDeleteCommand buildPhysicalSpecificationDeleteCommand(long specId) {
        return ImmutablePhysicalSpecificationDeleteCommand.builder()
                .specificationId(specId)
                .build();
    }

    private int deleteLogicalFlowDecorator(String username, Long logicalFlowId) {

        EntityReference entityReference = mkRef(LOGICAL_DATA_FLOW, logicalFlowId);

        List<DataTypeDecorator> logicalFlowDecoratorList = dataTypeDecoratorService.findByEntityId(entityReference);
        if (logicalFlowDecoratorList.isEmpty()) {
            LOG.debug("No decorators found for logicalFlowId={}", logicalFlowId);
            return 0;  // nothing to do
        }
        Set<Long> dataTypeIds = logicalFlowDecoratorList.stream()
                .map(d -> d.decoratorEntity().id())
                .collect(Collectors.toSet());

        int deleted = dataTypeDecoratorService.removeDataTypeDecorator(username, entityReference, dataTypeIds);
        LOG.info("Successfully deleted {} decorators for logicalFlowId={} by user={}", deleted, logicalFlowId, username);
        return deleted;
    }
}