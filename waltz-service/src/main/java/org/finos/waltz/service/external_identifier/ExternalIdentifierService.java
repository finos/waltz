/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.external_identifier;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.external_identifier.ExternalIdentifierDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.external_identifier.ExternalIdentifier;
import org.finos.waltz.model.external_identifier.ImmutableExternalIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.IntStream;

import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.SetUtilities.minus;

@Service
public class ExternalIdentifierService {

    private final ExternalIdentifierDao externalIdentifierDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public ExternalIdentifierService(ExternalIdentifierDao externalIdentifierDao,
                                     ChangeLogService changeLogService) {
        this.externalIdentifierDao = externalIdentifierDao;
        this.changeLogService = changeLogService;
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

    public int delete(EntityReference entityRef,
                      String externalId,
                      String provenance,
                      String username) {

        ImmutableExternalIdentifier externalIdentifier = ImmutableExternalIdentifier
                .builder()
                .externalId(externalId)
                .entityReference(entityRef)
                .system(provenance)
                .build();

        int deleteResult = externalIdentifierDao.delete(externalIdentifier);

        if (deleteResult > 0) {
            logChange(username,
                    entityRef,
                    String.format("Removed external id [%s] from entity id: %d",
                            externalId,
                            entityRef.id()),
                    Operation.UPDATE);
        }

        return deleteResult;
    }

    public int delete(EntityReference entityRef) {
        return externalIdentifierDao.delete(entityRef);
    }


    public int create(EntityReference entityRef,
                      String externalId,
                      String username) {

        ImmutableExternalIdentifier externalIdentifier = ImmutableExternalIdentifier
                .builder()
                .externalId(externalId)
                .entityReference(entityRef)
                .system("waltz")
                .build();

        int createResult = externalIdentifierDao.create(externalIdentifier);

        if (createResult > 0) {
            logChange(username,
                    entityRef,
                    String.format("Added external id [%s] to entity id: %d",
                            externalId,
                            entityRef.id()),
                    Operation.UPDATE);
        }
        return createResult;
    }

    private void logChange(String userId,
                           EntityReference ref,
                           String message,
                           Operation operation) {

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }
}
