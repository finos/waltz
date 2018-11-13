package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.model.taxonomy_management.ImmutableTaxonomyChangeCommand;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangePreview;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.indexBy;

@Service
public class TaxonomyChangeService {

    private final Map<TaxonomyChangeType, TaxonomyCommandProcessor> processorsByType;

    private Map<Long, TaxonomyChangeCommand> pendingCommandsHack;
    private AtomicLong commandCtrHack = new AtomicLong();

    @Autowired
    public TaxonomyChangeService(
            List<TaxonomyCommandProcessor> processors) {
        processorsByType = indexBy(p -> p.type(), processors);
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = getCommandProcessor(command);
        return processor.preview(command);
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand command, String userId) {
      TaxonomyCommandProcessor processor = getCommandProcessor(command);
      return processor.apply(command, userId);
    }


    public TaxonomyChangeCommand submitPendingChange(TaxonomyChangeCommand cmd) {
        TaxonomyChangeCommand pendingCmd = ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withId(commandCtrHack.getAndIncrement());

        pendingCommandsHack.put(pendingCmd.id().get(), pendingCmd);
        return pendingCmd;
    }

    public Collection<TaxonomyChangeCommand> getPendingChanges() {
        return pendingCommandsHack.values();
    }


    public TaxonomyChangeCommand applyById(long id, String userId) {
        TaxonomyChangeCommand command = pendingCommandsHack.get(id);
        return apply(command, userId);
    }

    private TaxonomyCommandProcessor getCommandProcessor(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = processorsByType.get(command.changeType());
        checkNotNull(processor, "Cannot find processor for type: %s", command.changeType());
        return processor;
    }


}
