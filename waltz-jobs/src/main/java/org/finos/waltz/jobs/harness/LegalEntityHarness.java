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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.BulkUploadLegalEntityRelationshipCommand;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.ImmutableBulkUploadLegalEntityRelationshipCommand;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.LegalEntityBulkUploadFixedColumns;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolveBulkUploadLegalEntityRelationshipResponse;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.bulk_upload.BulkUploadLegalEntityRelationshipService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static java.lang.String.format;


public class LegalEntityHarness {

    public static void main(String[] args) throws InterruptedException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        BulkUploadLegalEntityRelationshipService service = ctx.getBean(BulkUploadLegalEntityRelationshipService.class);

        String header = format("%s, %s, %s\n", LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.COMMENT);
        String inputString = header + "12345-1, 1234,, CLEJ";

        BulkUploadLegalEntityRelationshipCommand uploadCommand = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(1L)
                .build();

        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCommand);

        System.out.println(resolvedCommand.rows().size());

    }

}
