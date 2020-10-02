package com.khartec.waltz.jobs.example;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.IOUtilities.getFileResource;
import static com.khartec.waltz.schema.Tables.*;

@Service
public class GenericTaxonomyLoader {

    private final DSLContext dsl;

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        GenericTaxonomyLoader importer = ctx.getBean(GenericTaxonomyLoader.class);

        GenericTaxonomyLoadConfig config = ImmutableGenericTaxonomyLoadConfig.builder()
                .resourcePath("taxonomies/arch-review.tsv")
                .descriptionOffset(Columns.C)
                .taxonomyDescription("Application Architecture Reviews")
                .taxonomyExternalId("APP_ARCH")
                .taxonomyName("Application Architecture Reviews")
                .maxLevels(2)
                .ratingSchemeId(1L)
                .build();

        importer.go(config);
    }

    @Autowired
    public GenericTaxonomyLoader(DSLContext dsl) {
        this.dsl = dsl;
    }


    public void go(GenericTaxonomyLoadConfig config) throws IOException {
        List<String> lines = readLines(config);

        dsl.transaction(ctx -> {

            DSLContext tx = ctx.dsl();

            scrub(config, tx);

            Long categoryId = createCategory(config, tx);

            Timestamp creationTime = DateTimeUtilities.nowUtcTimestamp();

            int[] insertRcs = lines
                    .stream()
                    .filter(StringUtilities::notEmpty)
                    .map(line -> line.split("\\t"))
                    .flatMap(cells -> Stream.of(
                            mkMeasurableRecord(categoryId, cells[0], "", null, creationTime),
                            mkMeasurableRecord(categoryId, cells[1], cells[2], cells[0], creationTime)))
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), tx::batchInsert))
                    .execute();

            System.out.printf("Inserted %d records\n", insertRcs.length);

//            throw new RuntimeException("BOOooOOM!");
        });

    }


    private MeasurableRecord mkMeasurableRecord(Long categoryId, String name, String desc, String parentName, Timestamp creationTime) {
        MeasurableRecord r = new MeasurableRecord();
        r.setMeasurableCategoryId(categoryId);
        r.setName(name);
        r.setDescription(desc);
        r.setConcrete(true);
        r.setExternalId(toExtId(name));
        r.setLastUpdatedAt(creationTime);
        r.setLastUpdatedBy("admin");
        r.setProvenance("SAMPLE");

        if (parentName != null) {
            r.setExternalParentId(toExtId(parentName));
        }

        return r;
    }


    private String toExtId(String s) {
        return StringUtilities.mkSafe(s);
    }


    private Long createCategory(GenericTaxonomyLoadConfig config, DSLContext tx) {
        MeasurableCategoryRecord categoryRecord = tx.newRecord(MEASURABLE_CATEGORY);
        categoryRecord.setDescription(config.taxonomyDescription());
        categoryRecord.setName(config.taxonomyName());
        categoryRecord.setExternalId(config.taxonomyExternalId());
        categoryRecord.setRatingSchemeId(config.ratingSchemeId());

        categoryRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        categoryRecord.setEditable(true);
        categoryRecord.setLastUpdatedBy("admin");

        categoryRecord.insert();
        return categoryRecord.getId();
    }


    private void scrub(GenericTaxonomyLoadConfig config, DSLContext tx) {
        Long existingCategoryId = tx
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(config.taxonomyExternalId()))
                .fetchOne(MEASURABLE_CATEGORY.ID);

        Optional.ofNullable(existingCategoryId)
                .ifPresent(catId -> {
                    tx.deleteFrom(MEASURABLE_RATING)
                        .where(MEASURABLE_RATING.MEASURABLE_ID.in(DSL
                                .select(MEASURABLE.ID)
                                .from(MEASURABLE)
                                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(catId))))
                        .execute();

                    tx.deleteFrom(MEASURABLE)
                        .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(catId))
                        .execute();

                    tx.deleteFrom(MEASURABLE_CATEGORY)
                        .where(MEASURABLE_CATEGORY.ID.eq(catId))
                        .execute();
                });
    }

    private List<String> readLines(GenericTaxonomyLoadConfig config) throws IOException {
        Resource resource = getFileResource(config.resourcePath());
        List<String> lines = IOUtilities.readLines(resource.getInputStream());
        return lines;
    }


}
