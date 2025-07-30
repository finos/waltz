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

package org.finos.waltz.jobs.tools.importers.licence.finos;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.licence.LicenceDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.licence.Licence;
import org.finos.waltz.schema.tables.records.EntityNamedNoteRecord;
import org.finos.waltz.schema.tables.records.EntityNamedNoteTypeRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getYamlMapper;
import static org.finos.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;
import static org.finos.waltz.schema.tables.EntityNamedNoteType.ENTITY_NAMED_NOTE_TYPE;

public class FinosLicenceComplianceImporter {

    public static final String PROVENANCE = "finos";
    private static final String ENTITY_NOTE_CONDITIONS_NAME = "Conditions";
    private static final String ENTITY_NOTE_TERMINATIONS_NAME = "Termination Provisions";
    private static final String ENTITY_NOTE_VERSIONING_NAME = "Licence Versioning";
    private static final String ENTITY_NOTE_OTHER_NAME = "Other Terms";

    private static final String ENTITY_NOTE_CONDITIONS_DESC = "Condition of the license or active requirement that must be met for license compliance for the listed use-case(s)";
    private static final String ENTITY_NOTE_TERMINATIONS_DESC = "License termination clause";
    private static final String ENTITY_NOTE_VERSIONING_DESC = "Information related to how other versions of the same license may be applied";
    private static final String ENTITY_NOTE_OTHER_DESC = "Other clauses may modify conditions or trigger requirement on different use-cases than the four defined here";

    private final DSLContext dsl;
    private final LicenceDao licenceDao;


    public FinosLicenceComplianceImporter(DSLContext dsl, LicenceDao licenceDao) {
        this.dsl = dsl;
        this.licenceDao = licenceDao;
    }


    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ctx.register(FinosLicenceComplianceImporter.class);
        FinosLicenceComplianceImporter importer = ctx.getBean(FinosLicenceComplianceImporter.class);

        importer.doImport();
    }

    public void doImport() throws IOException, URISyntaxException {
        String path = "licence/finos";
        importData(path);
    }


    private void importData(String path) throws IOException, URISyntaxException {
        EntityNamedNoteTypeRecord conditionsNoteType = createEntityNoteDefinitionIfNotExists(
                ENTITY_NOTE_CONDITIONS_NAME,
                ENTITY_NOTE_CONDITIONS_DESC);

        EntityNamedNoteTypeRecord terminationsNoteType = createEntityNoteDefinitionIfNotExists(
                ENTITY_NOTE_TERMINATIONS_NAME,
                ENTITY_NOTE_TERMINATIONS_DESC);

        EntityNamedNoteTypeRecord versioningNoteType = createEntityNoteDefinitionIfNotExists(
                ENTITY_NOTE_VERSIONING_NAME,
                ENTITY_NOTE_VERSIONING_DESC);

        EntityNamedNoteTypeRecord otherNoteType = createEntityNoteDefinitionIfNotExists(
                ENTITY_NOTE_OTHER_NAME,
                ENTITY_NOTE_OTHER_DESC);

        deleteExisting();

        List<LicenceCompliance> compliances = parseData(path);

        System.out.printf("Parsed %s licence compliance files \n", compliances.size());

        Map<String, Licence> licencesByExternalId = licenceDao.findAll()
                .stream()
                .filter(l -> l.externalId().isPresent())
                .collect(toMap(l -> l.externalId().get(), l -> l));

        List<EntityNamedNoteRecord> notes = compliances.stream()
                .flatMap(c -> {
                    return Stream.of(c.licenseId())
                            .map(id -> Tuple.tuple(id, c));

                })
                .flatMap(t -> {
                    Map<ComplianceType, List<ComplianceTerm>> termsByType = Arrays.stream(t.v2.terms())
                            .collect(groupingBy(term -> term.type()));
                    return Stream.of(
                            maybeMkConditionNamedNote(
                                    termsByType.get(ComplianceType.CONDITION),
                                    licencesByExternalId.get(t.v1),
                                    conditionsNoteType),
                            maybeMkBulletNamedNote(
                                    termsByType.get(ComplianceType.TERMINATION),
                                    licencesByExternalId.get(t.v1),
                                    terminationsNoteType),
                            maybeMkBulletNamedNote(
                                    termsByType.get(ComplianceType.LICENSE_VERSIONS),
                                    licencesByExternalId.get(t.v1),
                                    versioningNoteType),
                            maybeMkOtherNamedNote(
                                    termsByType.get(ComplianceType.OTHER),
                                    licencesByExternalId.get(t.v1),
                                    otherNoteType)
                    );

                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        int[] noteStoreExecute = dsl.batchStore(notes).execute();

        System.out.println("Entity Note records stored: " + noteStoreExecute.length);

    }


    private Optional<EntityNamedNoteRecord> maybeMkConditionNamedNote(List<ComplianceTerm> conditions,
                                                                      Licence licence,
                                                                      EntityNamedNoteTypeRecord typeRecord) {
        checkNotNull(typeRecord, "typeRecord cannot be null");

        if(licence == null || conditions == null || conditions.isEmpty()) {
            return Optional.empty();
        }

        EntityNamedNoteRecord record = dsl.newRecord(ENTITY_NAMED_NOTE);
        record.setEntityId(licence.id().get());
        record.setEntityKind(EntityKind.LICENCE.name());
        record.setNamedNoteTypeId(typeRecord.getId());
        record.setProvenance(PROVENANCE);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");

        StringBuilder noteText = new StringBuilder();

        noteText
                .append("| Description | Unmodified Binary | Modified Binary | Unmodified Source | Modified Source | Compliance Notes |")
                .append("\n")
                .append("| --- |:---:|:---:|:---:|:---:| --- |")
                .append("\n");

        conditions.forEach(c -> {
            Set<ComplianceUseCase> complianceUseCases = SetUtilities.fromArray(c.useCases());

            noteText
                    .append(" | ").append(c.description())
                    .append(" | ").append(complianceUseCases.contains(ComplianceUseCase.UB) ? " X " : " ")
                    .append(" | ").append(complianceUseCases.contains(ComplianceUseCase.MB) ? " X " : " ")
                    .append(" | ").append(complianceUseCases.contains(ComplianceUseCase.US) ? " X " : " ")
                    .append(" | ").append(complianceUseCases.contains(ComplianceUseCase.MS) ? " X " : " ")
                    .append(" | ").append(c.complianceNotes())
                    .append(" | ")
                    .append("\n");

        });
        record.setNoteText(noteText.toString());
        return Optional.of(record);
    }


    private Optional<EntityNamedNoteRecord> maybeMkBulletNamedNote(List<ComplianceTerm> terms,
                                                                   Licence licence,
                                                                   EntityNamedNoteTypeRecord typeRecord) {
        checkNotNull(typeRecord, "typeRecord cannot be null");

        if(licence == null || terms == null || terms.isEmpty()) {
            return Optional.empty();
        }

        EntityNamedNoteRecord record = dsl.newRecord(ENTITY_NAMED_NOTE);
        record.setEntityId(licence.id().get());
        record.setEntityKind(EntityKind.LICENCE.name());
        record.setNamedNoteTypeId(typeRecord.getId());
        record.setProvenance(PROVENANCE);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");

        StringBuilder noteText = new StringBuilder();

        noteText
                .append("| Description |")
                .append("\n")
                .append("| --- |")
                .append("\n");

        terms.forEach(c -> {
            noteText
                    .append(" | ").append(c.description())
                    .append(" | ")
                    .append("\n");

        });

        record.setNoteText(noteText.toString());
        return Optional.of(record);
    }


    private Optional<EntityNamedNoteRecord> maybeMkOtherNamedNote(List<ComplianceTerm> terms,
                                                                  Licence licence,
                                                                  EntityNamedNoteTypeRecord typeRecord) {
        checkNotNull(typeRecord, "typeRecord cannot be null");

        if(licence == null || terms == null || terms.isEmpty()) {
            return Optional.empty();
        }

        EntityNamedNoteRecord record = dsl.newRecord(ENTITY_NAMED_NOTE);
        record.setEntityId(licence.id().get());
        record.setEntityKind(EntityKind.LICENCE.name());
        record.setNamedNoteTypeId(typeRecord.getId());
        record.setProvenance(PROVENANCE);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");

        StringBuilder noteText = new StringBuilder();

        noteText
                .append("| Description | Compliance Notes |")
                .append("\n")
                .append("| --- | --- |")
                .append("\n");

        terms.forEach(c -> {
            noteText
                    .append(" | ").append(c.description())
                    .append(" | ").append(c.complianceNotes())
                    .append(" | ")
                    .append("\n");

        });

        record.setNoteText(noteText.toString());
        return Optional.of(record);
    }


    private EntityNamedNoteTypeRecord createEntityNoteDefinitionIfNotExists(String name, String description) {
        checkNotEmpty(name, "name must be provided");
        checkNotEmpty(description, "description must be provided");

        EntityNamedNoteTypeRecord existingRecord = dsl.selectFrom(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.NAME.eq(name))
                .fetchOne();

        if(existingRecord != null) {
            return existingRecord;
        }

        EntityNamedNoteTypeRecord record = dsl.newRecord(ENTITY_NAMED_NOTE_TYPE);
        record.setApplicableEntityKinds(EntityKind.LICENCE.name());

        record.setName(name);
        record.setDescription(description);
        record.setIsReadonly(true);

        record.insert();
        return record;
    }





    private List<LicenceCompliance> parseData(String directoryPath) throws IOException, URISyntaxException {
        URI directoryUrl = this.getClass().getClassLoader().getResource(directoryPath).toURI();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryUrl))) {

            List<LicenceCompliance> compliances = paths
                    .filter(Files::isRegularFile)
                    .map(this::parseCompliance)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());

            System.out.printf("Parsed %s FINOS licence files \n", compliances.size());
            return compliances;
        }
    }


    private Optional<LicenceCompliance> parseCompliance(Path path) {
        try {
            LicenceCompliance compliance = getYamlMapper().readValue(path.toFile(), LicenceCompliance.class);
            return Optional.of(compliance);
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    private void deleteExisting() {
        int deleteCount = dsl.deleteFrom(ENTITY_NAMED_NOTE)
                .where(ENTITY_NAMED_NOTE.PROVENANCE.eq(PROVENANCE))
                .execute();

        System.out.printf("Deleted %s compliance notes \n", deleteCount);
    }

}
