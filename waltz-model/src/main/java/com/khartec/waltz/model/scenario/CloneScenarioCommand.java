package com.khartec.waltz.model.scenario;

import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CloneScenarioCommand implements Command {
    public abstract long scenarioId();
    public abstract String userId();
    public abstract String newName();
}
