package com.khartec.waltz.data.physical_data_article;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableProduceConsumeGroup;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_data_article.DataFormatKind;
import com.khartec.waltz.model.physical_data_article.ImmutablePhysicalDataArticle;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import com.khartec.waltz.schema.tables.records.PhysicalDataArticleRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalDataArticle.PHYSICAL_DATA_ARTICLE;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static java.util.Collections.emptyList;

@Repository
public class PhysicalDataArticleDao {

    public static final RecordMapper<? super Record, PhysicalDataArticle> TO_DOMAIN_MAPPER = r -> {
        PhysicalDataArticleRecord record = r.into(PHYSICAL_DATA_ARTICLE);
        return ImmutablePhysicalDataArticle.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .owningApplicationId(record.getOwningApplicationId())
                .name(record.getName())
                .description(record.getDescription())
                .format(DataFormatKind.valueOf(record.getFormat()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalDataArticleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalDataArticle> findByProducerAppId(long appId) {
        return findByProducerAppIdQuery(appId)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalDataArticle> findByConsumerAppId(long appId) {
        return findByConsumerAppIdQuery(appId)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public ProduceConsumeGroup<PhysicalDataArticle> findByAppId(long appId) {

        List<Tuple2<String, PhysicalDataArticle>> results = findByProducerAppIdQuery(appId)
                .unionAll(findByConsumerAppIdQuery(appId))
                .fetch(r -> Tuple.tuple(
                        r.getValue("relationship", String.class),
                        TO_DOMAIN_MAPPER.map(r)));

        Map<String, Collection<PhysicalDataArticle>> groupedResults = groupBy(
                t -> t.v1, // relationship
                t -> t.v2, // article
                results);

        return ImmutableProduceConsumeGroup.<PhysicalDataArticle>builder()
                .produces(groupedResults.getOrDefault("producer", emptyList()))
                .consumes(groupedResults.getOrDefault("consumer", emptyList()))
                .build();
    }


    public PhysicalDataArticle getById(long id) {
        return dsl
                .select(PHYSICAL_DATA_ARTICLE.fields())
                .from(PHYSICAL_DATA_ARTICLE)
                .where(PHYSICAL_DATA_ARTICLE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<PhysicalDataArticle> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(PHYSICAL_DATA_ARTICLE.fields())
                .from(PHYSICAL_DATA_ARTICLE)
                .where(PHYSICAL_DATA_ARTICLE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private Select<Record> findByProducerAppIdQuery(long appId) {
        return dsl
                .select(DSL.value("producer").as("relationship"))
                .select(PHYSICAL_DATA_ARTICLE.fields())
                .from(PHYSICAL_DATA_ARTICLE)
                .where(PHYSICAL_DATA_ARTICLE.OWNING_APPLICATION_ID.eq(appId));
    }


    private Select<Record> findByConsumerAppIdQuery(long appId) {
        return dsl
                .select(DSL.value("consumer").as("relationship"))
                .select(PHYSICAL_DATA_ARTICLE.fields())
                .from(PHYSICAL_DATA_ARTICLE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ARTICLE_ID.eq(PHYSICAL_DATA_ARTICLE.ID))
                .innerJoin(DATA_FLOW)
                .on(PHYSICAL_FLOW.FLOW_ID.eq(DATA_FLOW.ID))
                .where(DATA_FLOW.TARGET_ENTITY_ID.eq(appId))
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
    }

}
