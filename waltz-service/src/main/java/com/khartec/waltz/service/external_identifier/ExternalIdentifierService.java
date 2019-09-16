package com.khartec.waltz.service.external_identifier;

import com.khartec.waltz.data.external_identifier.ExternalIdentifierDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.external_identifier.ImmutableExternalIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ExternalIdentifierService {

    private final ExternalIdentifierDao externalIdentifierDao;

    @Autowired
    public ExternalIdentifierService(ExternalIdentifierDao externalIdentifierDao) {
        this.externalIdentifierDao = externalIdentifierDao;
    }

    public int markEntityRefAsDuplicate(EntityReference toBeDuplicated, EntityReference duplicatedBy) {
        List<ExternalIdentifier> identifiersToBeDuplicated = externalIdentifierDao.findByEntityReference(toBeDuplicated);
        List<ExternalIdentifier> identifiersDuplicatedBy = externalIdentifierDao.findByEntityReference(duplicatedBy);

        Set<ExternalIdentifier> externalIdentifiersToBeCreated = identifiersToBeDuplicated
                .stream()
                .map(id -> updateEntityReference(id, duplicatedBy))
                .filter(id -> !identifiersDuplicatedBy.contains(id))
                .collect(Collectors.toSet());

        int[] createResult = externalIdentifierDao.create(externalIdentifiersToBeCreated);
        int[] deleteResult = externalIdentifierDao.delete(identifiersToBeDuplicated);

        return IntStream.of(createResult).sum() + IntStream.of(deleteResult).sum();
    }

    private ExternalIdentifier updateEntityReference(ExternalIdentifier id, EntityReference duplicatedBy) {
        return ImmutableExternalIdentifier.builder()
                .entityReference(duplicatedBy)
                .externalId(id.externalId())
                .system(id.system())
                .build();
    }
}
