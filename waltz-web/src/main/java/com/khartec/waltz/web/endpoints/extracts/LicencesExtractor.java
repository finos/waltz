package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.khartec.waltz.model.application.ApplicationIdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.Tables.LICENCE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class LicencesExtractor extends BaseDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    @Autowired
    public LicencesExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "licences", ":kind", ":id");
        get(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);

            ApplicationIdSelectionOptions selectionOptions = mkOpts(entityRef);
            Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(selectionOptions);

            SelectConditionStep<Record8<Long, String, String, String, String, Timestamp, String, String>> qry = dsl
                    .selectDistinct(LICENCE.ID.as("Licence Id"),
                            LICENCE.NAME.as("Licence Name"),
                            LICENCE.DESCRIPTION.as("Description"),
                            LICENCE.EXTERNAL_ID.as("External Id"),
                            LICENCE.APPROVAL_STATUS.as("Approval Status"),
                            LICENCE.LAST_UPDATED_AT.as("Last Updated At"),
                            LICENCE.LAST_UPDATED_BY.as("Last Updated By"),
                            LICENCE.PROVENANCE.as("Provenance"))
                    .from(LICENCE)
                    .innerJoin(ENTITY_RELATIONSHIP)
                    .on(LICENCE.ID.eq(ENTITY_RELATIONSHIP.ID_B).and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.LICENCE.name())))
                    .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(ENTITY_RELATIONSHIP.ID_A).and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name())))
                    .where(APPLICATION.ID.in(appIds))
                    .and(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"));


            String filename = "licences";

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }
}
