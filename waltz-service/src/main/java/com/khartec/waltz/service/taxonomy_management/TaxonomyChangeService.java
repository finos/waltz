package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.taxonomy_management.TaxonomyChangeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.client_cache_key.ClientCacheKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static java.util.stream.Collectors.toMap;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class TaxonomyChangeService {

    private final TaxonomyChangeDao taxonomyChangeDao;
    private final Map<TaxonomyChangeType, TaxonomyCommandProcessor> processorsByType;
    private final ClientCacheKeyService clientCacheKeyService;


    @Autowired
    public TaxonomyChangeService(
            TaxonomyChangeDao taxonomyChangeDao,
            ClientCacheKeyService clientCacheKeyService,
            List<TaxonomyCommandProcessor> processors) {
        checkNotNull(taxonomyChangeDao, "taxonomyChangeDao cannot be null");
        checkNotNull(clientCacheKeyService, "clientCacheKeyService cannot be null");
        this.clientCacheKeyService = clientCacheKeyService;
        this.taxonomyChangeDao = taxonomyChangeDao;
        processorsByType = processors
                .stream()
                .flatMap(p -> p.supportedTypes()
                        .stream()
                        .map(st -> tuple(st, p)))
                .collect(toMap(t -> t.v1, t -> t.v2));
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = getCommandProcessor(command);
        return processor.preview(command);
    }


    public TaxonomyChangePreview previewById(long id) {
        TaxonomyChangeCommand command = taxonomyChangeDao.getDraftCommandById(id);
        return preview(command);
    }


    private TaxonomyChangeCommand apply(TaxonomyChangeCommand command, String userId) {
      TaxonomyCommandProcessor processor = getCommandProcessor(command);
      return processor.apply(command, userId);
    }


    public TaxonomyChangeCommand submitDraftChange(TaxonomyChangeCommand draftCommand, String userId) {
        checkTrue(draftCommand.status() == TaxonomyChangeLifecycleStatus.DRAFT, "Command must be DRAFT");

        TaxonomyChangeCommand commandToSave = ImmutableTaxonomyChangeCommand
                .copyOf(draftCommand)
                .withCreatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withCreatedBy(userId);

        return taxonomyChangeDao
                .createCommand(commandToSave);
    }


    public Collection<TaxonomyChangeCommand> findDraftChangesByDomain(EntityReference domain) {
        return taxonomyChangeDao
                .findChangesByDomainAndStatus(domain, TaxonomyChangeLifecycleStatus.DRAFT);
    }


    public TaxonomyChangeCommand applyById(long id, String userId) {
        TaxonomyChangeCommand command = taxonomyChangeDao.getDraftCommandById(id);
        TaxonomyChangeCommand updatedCommand = apply(command, userId);
        clientCacheKeyService.createOrUpdate("TAXONOMY");
        return taxonomyChangeDao.update(updatedCommand);
    }


    private TaxonomyCommandProcessor getCommandProcessor(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = processorsByType.get(command.changeType());
        checkNotNull(processor, "Cannot find processor for type: %s", command.changeType());
        return processor;
    }


    public boolean removeById(long id, String userId) {
        return taxonomyChangeDao.removeById(id, userId);
    }
}
