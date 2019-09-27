package com.khartec.waltz.service.external_identifier;

import com.khartec.waltz.data.external_identifier.ExternalIdentifierDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.external_identifier.ImmutableExternalIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.common.SetUtilities.minus;

@Service
public class ExternalIdentifierService {

    private final ExternalIdentifierDao externalIdentifierDao;


    @Autowired
    public ExternalIdentifierService(ExternalIdentifierDao externalIdentifierDao) {
        this.externalIdentifierDao = externalIdentifierDao;
    }


    public Set<ExternalIdentifier> findByEntityReference(EntityReference entityRef) {
        return externalIdentifierDao.findByEntityReference(entityRef);
    }


    public Set<ExternalIdentifier> findByKind(EntityKind kind, String extId) {
        return externalIdentifierDao.findByKind(kind, extId);
    }


    public int merge(EntityReference fromRef,
                     EntityReference toRef) {

        Set<ExternalIdentifier> existingIdentifiersOnSource = findByEntityReference(fromRef);
        Set<ExternalIdentifier> existingIdentifiersOnTarget = findByEntityReference(toRef);

        Set<ExternalIdentifier> identifiersToCopyFromSource = map(
                existingIdentifiersOnSource,
                existingIdentifier -> ImmutableExternalIdentifier
                        .copyOf(existingIdentifier)
                        .withEntityReference(toRef));

        Set<ExternalIdentifier> identifiersToCreate = minus(
                identifiersToCopyFromSource,
                existingIdentifiersOnTarget);

        int[] createResult = externalIdentifierDao.create(identifiersToCreate);
        int[] deleteResult = externalIdentifierDao.delete(existingIdentifiersOnSource);

        return IntStream.of(createResult).sum() + IntStream.of(deleteResult).sum();
    }

    public int updateExternalIdentifiers(EntityReference entityRef, List<String> externalIds) {
        Set<ExternalIdentifier> externalIdToRemove = findByEntityReference(entityRef)
                .stream()
                .filter(ei -> !externalIds.contains(ei.externalId()))
                .collect(Collectors.toSet());

        int[] deleteResult = externalIdentifierDao.delete(externalIdToRemove);
        return IntStream.of(deleteResult).sum();
    }
}
