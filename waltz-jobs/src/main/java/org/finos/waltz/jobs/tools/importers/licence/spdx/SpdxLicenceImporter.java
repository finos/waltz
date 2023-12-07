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

package org.finos.waltz.jobs.tools.importers.licence.spdx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.licence.LicenceDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.records.BookmarkRecord;
import org.finos.waltz.schema.tables.records.LicenceRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.schema.tables.Bookmark.BOOKMARK;
import static org.finos.waltz.schema.tables.Licence.LICENCE;

public class SpdxLicenceImporter {

    public static final String PROVENANCE = "spdx";
    private static final String SPDX_LICENCE_TEMPLATE_URL = "https://spdx.org/licenses/%s.html";

    private final DSLContext dsl;
    private final LicenceDao licenceDao;
    private final ObjectMapper mapper;


    public SpdxLicenceImporter(DSLContext dsl, LicenceDao licenceDao) {

        this.dsl = dsl;
        this.licenceDao = licenceDao;

        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }


    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ctx.register(SpdxLicenceImporter.class);
        SpdxLicenceImporter importer = ctx.getBean(SpdxLicenceImporter.class);
        importer.doImport();
    }

    public void doImport() throws IOException, URISyntaxException {
        String path = "licence/spdx/details";
        importData(path);
    }

    private void importData(String path) throws IOException, URISyntaxException {

        List<SpdxLicence> spdxLicences = parseData(path);
        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        // fetching existing licences
        Set<String> existingExternalIds = licenceDao.findAll()
                .stream()
                .filter(l -> l.externalId() != null)
                .map(l -> l.externalId().get().toLowerCase())
                .collect(Collectors.toSet());

        // add only new ones
        List<LicenceRecord> records = spdxLicences.stream()
                .filter(l -> !existingExternalIds.contains(l.licenseId().toLowerCase()))
                .map(l -> {
                    LicenceRecord record = dsl.newRecord(LICENCE);
                    record.setName(l.name());
                    record.setExternalId(l.licenseId());
                    record.setProvenance(PROVENANCE);
                    record.setCreatedAt(now);
                    record.setCreatedBy("admin");
                    record.setLastUpdatedAt(now);
                    record.setLastUpdatedBy("admin");
                    return record;
                })
                .collect(toList());

        int[] execute = dsl.batchStore(records).execute();

        System.out.println("Licence records stored to database: " + execute.length);


        // now create bookmarks from seeAlso section
        deleteExistingBookmarks();

        Map<String, LicenceRecord> licencesByExternalId = dsl
                .selectFrom(LICENCE)
                .where(LICENCE.EXTERNAL_ID.isNotNull())
                .fetch()
                .stream()
                .collect(Collectors.toMap(l -> l.getExternalId(), l -> l));

        List<BookmarkRecord> bookmarks = spdxLicences.stream()
                .flatMap(l -> {
                    Long licenceId = licencesByExternalId.get(l.licenseId()).getId();

                    Stream<BookmarkRecord> stream = Stream
                            .of(l.seeAlso())
                            .map(url -> {
                                BookmarkRecord bookmarkRecord = dsl.newRecord(BOOKMARK);
                                bookmarkRecord.setTitle("See Also");
                                bookmarkRecord.setKind("DOCUMENTATION");
                                bookmarkRecord.setUrl(url);
                                bookmarkRecord.setParentKind(EntityKind.LICENCE.name());
                                bookmarkRecord.setParentId(licenceId);
                                bookmarkRecord.setIsPrimary(false);
                                bookmarkRecord.setProvenance(PROVENANCE);
                                bookmarkRecord.setLastUpdatedBy("admin");
                                bookmarkRecord.setCreatedAt(now);
                                bookmarkRecord.setUpdatedAt(now);
                                bookmarkRecord.setIsRequired(false);
                                return bookmarkRecord;
                            });

                    BookmarkRecord spdxRecord = dsl.newRecord(BOOKMARK);
                    spdxRecord.setTitle("SPDX");
                    spdxRecord.setKind("DOCUMENTATION");
                    spdxRecord.setUrl(String.format(SPDX_LICENCE_TEMPLATE_URL, l.licenseId()));
                    spdxRecord.setParentKind(EntityKind.LICENCE.name());
                    spdxRecord.setParentId(licenceId);
                    spdxRecord.setIsPrimary(false);
                    spdxRecord.setProvenance(PROVENANCE);
                    spdxRecord.setLastUpdatedBy("admin");
                    spdxRecord.setCreatedAt(now);
                    spdxRecord.setUpdatedAt(now);
                    spdxRecord.setIsRequired(false);

                    return Stream.concat(stream, Stream.of(spdxRecord));
                })
                .collect(toList());

        int[] bookmarkStoreExecute = dsl.batchStore(bookmarks).execute();

        System.out.println("Bookmark records stored: " + bookmarkStoreExecute.length);
    }


    private void deleteExistingBookmarks() {
        int bookmarkDeleteCount = dsl.deleteFrom(BOOKMARK)
                .where(BOOKMARK.PROVENANCE.eq(PROVENANCE))
                .execute();

        System.out.printf("Deleted %s licence bookmarks \n", bookmarkDeleteCount);
    }


    private List<SpdxLicence> parseData(String directoryPath) throws IOException, URISyntaxException {
        URI directoryUrl = this.getClass().getClassLoader().getResource(directoryPath).toURI();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryUrl))) {

            List<SpdxLicence> spdxLicences = paths
                    .filter(Files::isRegularFile)
                    .map(this::parseSpdxLicence)
                    .filter(l -> l.isPresent())
                    .map(l -> l.get())
                    .collect(toList());

            System.out.printf("Parsed %s SPDX licence files \n", spdxLicences.size());
            return spdxLicences;
        }
    }


    private Optional<SpdxLicence> parseSpdxLicence(Path path) {

        try {
            System.out.println("Parsing: " + path);
            SpdxLicence spdxLicence = mapper.readValue(path.toFile(), SpdxLicence.class);
            return Optional.of(spdxLicence);
        } catch (IOException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

}
