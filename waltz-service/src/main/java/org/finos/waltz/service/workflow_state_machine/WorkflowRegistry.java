package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A central registry for all workflow state machines in the application.
 * This service discovers all {@link WorkflowDefinition} beans at startup, builds a state machine for each one,
 * and stores them in a map for easy retrieval.
 */
@Service
public class WorkflowRegistry {

    private final List<WorkflowDefinition<?, ?, ?>> definitions;
    private final Map<EntityKind, WorkflowStateMachine<?, ?, ?>> machineRegistry = new ConcurrentHashMap<>();

    // the DAOs would be injected here and passed to the builder
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Autowired
    public WorkflowRegistry(List<WorkflowDefinition<?, ?, ?>> definitions,
                            EntityWorkflowStateDao entityWorkflowStateDao, EntityWorkflowTransitionDao entityWorkflowTransitionDao) {
        this.definitions = definitions;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
    }

    /**
     * This method is automatically called after the bean is constructed and all dependencies are injected.
     * It iterates through all discovered workflow definitions, builds a state machine for each, and registers it.
     */
    @PostConstruct
    public void buildMachines() {
        for (WorkflowDefinition<?, ?, ?> definition : definitions) {
            WorkflowStateMachineBuilder builder = new WorkflowStateMachineBuilder(entityWorkflowStateDao, entityWorkflowTransitionDao);
            WorkflowStateMachine machine = definition.build(builder);
            machineRegistry.put(definition.getEntityKind(), machine);
        }
    }

    /**
     * Retrieves the state machine for a given entity kind.
     *
     * @param kind The entity kind for which to retrieve the state machine.
     * @return The corresponding {@link WorkflowStateMachine}.
     * @throws IllegalArgumentException if no state machine is registered for the given entity kind.
     */
    public WorkflowStateMachine<?, ?, ?> getMachine(EntityKind kind) {
        WorkflowStateMachine<?, ?, ?> machine = machineRegistry.get(kind);
        if (machine == null) {
            throw new IllegalArgumentException("No workflow state machine registered for entity kind: " + kind);
        }
        return machine;
    }
}
