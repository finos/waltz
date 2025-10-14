package org.finos.waltz.service.data_flow;

import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.proposed_flow.ImmutableLogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class DataFlowService {
    private static final Logger LOG = LoggerFactory.getLogger(DataFlowService.class);

    private final ProposedFlowDao proposedFlowDao;
    public final LogicalFlowService logicalFlowService;
    public final PhysicalFlowService physicalFlowService;

    @Autowired
    public DataFlowService(ProposedFlowDao proposedFlowDao, LogicalFlowService logicalFlowService, PhysicalFlowService physicalFlowService) {
        this.proposedFlowDao = proposedFlowDao;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowService = physicalFlowService;
    }

    /**
     * Creates logical and physical flows from the ProposedFlow.
     *
     * @param proposedFlowId primary key of the ProposedFlow
     * @param username       actor requesting the creation
     * @return immutable response containing the created flows
     * @throws FlowCreationException if either creation step fails
     */
    public LogicalPhysicalFlowCreationResponse createLogicalAndPhysicalFlowFromProposedFlowDef(long proposedFlowId, String username) throws FlowCreationException {

        Objects.requireNonNull(username, "username must not be null");
        PhysicalFlowCreateCommandResponse physicalFlow;
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
        }

        //create physical flow
        physicalFlow = createPhysicalFlow(proposedFlow, username, logicalFlow.id());

        LOG.info("Successfully created flows for proposedFlowId = {}", proposedFlowId);

        return ImmutableLogicalPhysicalFlowCreationResponse.builder()
                .logicalFlow(logicalFlow)
                .physicalFlowCreateCommandResponse(physicalFlow)
                .build();
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

    public PhysicalFlowCreateCommand mapProposedFlowCommandToPhysicalFlowCreateCommand(ProposedFlowCommand command) {
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

    public Long getPhysicalFlowIfExist(ProposedFlowCommand command, String username) {
        return physicalFlowService.getPhysicalFlowIfExist(mapProposedFlowCommandToPhysicalFlowCreateCommand(command),
                username);
    }
}