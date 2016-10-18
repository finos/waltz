package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.data.physical_data_article.PhysicalDataArticleDao;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.randomPick;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalDataArticle.PHYSICAL_DATA_ARTICLE;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


public class PhysicalFlowGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<PhysicalDataArticle> articles = dsl.select(PHYSICAL_DATA_ARTICLE.fields())
                .from(PHYSICAL_DATA_ARTICLE)
                .fetch(PhysicalDataArticleDao.TO_DOMAIN_MAPPER);

        List<Tuple2<Long, Long>> allLogicalFLows = dsl.select(DATA_FLOW.ID, DATA_FLOW.SOURCE_ENTITY_ID)
                .from(DATA_FLOW)
                .fetch(r -> Tuple.tuple(r.getValue(DATA_FLOW.ID), r.getValue(DATA_FLOW.SOURCE_ENTITY_ID)));

        Map<Long, Collection<Long>> logicalByApp = groupBy(
                t -> t.v2(),
                t -> t.v1(),
                allLogicalFLows);

        List<PhysicalFlowRecord> records = articles.stream()
                .map(a -> {
                    Collection<Long> flowIds = logicalByApp.get(a.owningApplicationId());
                    if (isEmpty(flowIds)) return null;
                    return Tuple.tuple(a.id().get(), flowIds);
                })
                .filter(t -> t != null)
                .flatMap(t -> {
                    List<Long> flowIds = new LinkedList(t.v2);
                    return IntStream.range(0, t.v2.size() - 1)
                        .mapToObj(i -> {
                            PhysicalFlowRecord record = dsl.newRecord(PHYSICAL_FLOW);
                            record.setArticleId(t.v1);
                            record.setFlowId(flowIds.remove(rnd.nextInt(flowIds.size() - 1)));
                            record.setDescription("Description: " + t.v1 + " - " + t.v2);
                            record.setProvenance("DEMO");
                            record.setBasisOffset(randomPick(newArrayList(0, 0, 0, 0, 1, 1, 2, -1)));
                            record.setTransport(ArrayUtilities.randomPick(TransportKind.values()).name());
                            record.setFrequency(ArrayUtilities.randomPick(FrequencyKind.values()).name());
                            return record;
                        });
                })
                .collect(Collectors.toList());

        System.out.println("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.PROVENANCE.eq("DEMO"))
                .execute();

        System.out.println("---saving record: "+records.size());
        dsl.batchInsert(records).execute();

        System.out.println("---done");


    }
}
