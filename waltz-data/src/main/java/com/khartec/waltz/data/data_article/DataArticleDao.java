package com.khartec.waltz.data.data_article;

import com.khartec.waltz.model.data_article.DataArticle;
import com.khartec.waltz.model.data_article.DataFormat;
import com.khartec.waltz.model.data_article.ImmutableDataArticle;
import com.khartec.waltz.schema.tables.records.DataArticleRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.DataArticle.DATA_ARTICLE;

@Repository
public class DataArticleDao {

    private static final RecordMapper<? super Record, DataArticle> TO_DOMAIN = r -> {
        DataArticleRecord record = r.into(DATA_ARTICLE);
        return ImmutableDataArticle.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .owningApplicationId(record.getOwningApplicationId())
                .name(record.getName())
                .description(record.getDescription())
                .format(DataFormat.valueOf(record.getFormat()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public DataArticleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DataArticle> findForAppId(long id) {
        return dsl.select(DATA_ARTICLE.fields())
                .from(DATA_ARTICLE)
                .where(DATA_ARTICLE.OWNING_APPLICATION_ID.eq(id))
                .fetch(TO_DOMAIN);
    }
}
