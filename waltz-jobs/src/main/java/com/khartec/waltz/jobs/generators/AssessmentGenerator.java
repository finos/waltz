package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ColorUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.common.StreamUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.schema.tables.records.AssessmentDefinitionRecord;
import com.khartec.waltz.schema.tables.records.AssessmentRatingRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeItemRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AssessmentGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        RatingScheme ratingScheme = getOrCreateConfidentialityRatingScheme(ctx);
        Long defnId = createAssessmentDefinition(ctx, ratingScheme);
        createAssessmentRecords(ctx, ratingScheme, defnId);
        return null;
    }


    private void createAssessmentRecords(ApplicationContext ctx, RatingScheme ratingScheme, Long defnId) {
        ApplicationService appService = ctx.getBean(ApplicationService.class);
        List<Application> allApps = appService.findAll();


        List<AssessmentRatingRecord> records = allApps.stream()
                .filter(d -> RandomUtilities.getRandom().nextBoolean())
                .map(d -> tuple(d.id(), randomPick(ratingScheme.ratings())))
                .filter(t -> t.v1.isPresent())
                .filter(t -> t.v2.id().isPresent())
                .map(t -> {
                    AssessmentRatingRecord record = new AssessmentRatingRecord();
                    record.setAssessmentDefinitionId(defnId);
                    record.setRatingId(t.v2.id().get());
                    record.setEntityKind(EntityKind.APPLICATION.name());
                    record.setEntityId(t.v1.get());
                    record.setLastUpdatedBy(SAMPLE_DATA_USER);
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setDescription("sample data");
                    return record;
                })
                .collect(toList());

        getDsl(ctx).batchInsert(records).execute();
    }


    private Long createAssessmentDefinition(ApplicationContext ctx, RatingScheme ratingScheme) {
        AssessmentDefinitionRecord defnRecord = getDsl(ctx).newRecord(ASSESSMENT_DEFINITION);

        defnRecord.setName("Sensitive Data");
        defnRecord.setDescription("Indicates if the application contains sensitive data");
        defnRecord.setEntityKind(EntityKind.APPLICATION.name());
        defnRecord.setIsReadonly(true);
        defnRecord.setLastUpdatedBy(SAMPLE_DATA_USER);
        defnRecord.setProvenance(SAMPLE_DATA_PROVENANCE);
        defnRecord.setRatingSchemeId(ratingScheme
                .id()
                .orElseThrow(() -> new RuntimeException("Could not find rating scheme")));

        defnRecord.insert();

        return defnRecord.getId();
    }


    private RatingScheme getOrCreateConfidentialityRatingScheme(ApplicationContext ctx) {


        Long schemeId = getDsl(ctx)
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .where(RATING_SCHEME.NAME.eq("Confidentiality"))
                .fetchOne(RATING_SCHEME.ID);

        if (schemeId == null) {
            schemeId = createRatingScheme(ctx);
        }


        RatingSchemeService ratingSchemeService = ctx.getBean(RatingSchemeService.class);
        RatingScheme ratingScheme = ratingSchemeService.getById(schemeId);

        return ratingScheme;
    }


    private Long createRatingScheme(ApplicationContext ctx) {
        // create
        RatingSchemeRecord ratingSchemeRecord = getDsl(ctx).newRecord(RATING_SCHEME);
        ratingSchemeRecord.setName("Confidentiality");
        ratingSchemeRecord.setDescription("Confidentiality ratings");
        ratingSchemeRecord.insert();

        System.out.println("Inserted scheme "+ ratingSchemeRecord.getId());

        List<RatingSchemeItemRecord> ratingRecords = Stream
                .of(tuple("C", "Confidential", ColorUtilities.HexStrings.AMBER),
                    tuple("S", "Strictly Confidential", ColorUtilities.HexStrings.RED),
                    tuple("I", "Internal", ColorUtilities.HexStrings.BLUE),
                    tuple("P", "Public", ColorUtilities.HexStrings.GREEN),
                    tuple("U", "Unrated", ColorUtilities.HexStrings.GREY))
                .map(t -> {
                    RatingSchemeItemRecord itemR = getDsl(ctx).newRecord(RATING_SCHEME_ITEM);
                    itemR.setCode(t.v1);
                    itemR.setName(t.v2);
                    itemR.setColor(t.v3);
                    itemR.setDescription(t.v2);
                    itemR.setSchemeId(ratingSchemeRecord.getId());
                    itemR.setUserSelectable(true);
                    return itemR;
                })
                .collect(toList());

        getDsl(ctx)
                .batchInsert(ratingRecords)
                .execute();
        return ratingSchemeRecord.getId();
    }


    @Override
    public boolean remove(ApplicationContext ctx) {

        Condition condition = ASSESSMENT_DEFINITION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE);
        SelectConditionStep<Record1<Long>> definitionSelector = DSL
                .select(ASSESSMENT_DEFINITION.ID)
                .from(ASSESSMENT_DEFINITION)
                .where(condition);

        // delete ratings
        getDsl(ctx)
                .deleteFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.in(definitionSelector))
                .execute();


        // delete defns
        getDsl(ctx)
                .deleteFrom(ASSESSMENT_DEFINITION)
                .where(condition)
                .execute();

        return true;
    }

}
