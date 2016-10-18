package com.khartec.waltz.data.physical_data_article;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.LineageReportContributor.LINEAGE_REPORT_CONTRIBUTOR;
import static com.khartec.waltz.schema.tables.PhysicalDataArticle.PHYSICAL_DATA_ARTICLE;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


@Service
public class PhysicalDataArticleSelectorFactory implements IdSelectorFactory {

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case LINEAGE_REPORT:
                return mkForLineageReport(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical data article selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForLineageReport(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL.select(PHYSICAL_DATA_ARTICLE.ID)
                .from(PHYSICAL_DATA_ARTICLE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ARTICLE_ID.eq(PHYSICAL_DATA_ARTICLE.ID))
                .innerJoin(LINEAGE_REPORT_CONTRIBUTOR)
                .on(LINEAGE_REPORT_CONTRIBUTOR.PHYSICAL_FLOW_ID.eq(PHYSICAL_FLOW.ID))
                .where(LINEAGE_REPORT_CONTRIBUTOR.LINEAGE_REPORT_ID.eq(options.entityReference().id()));
    }

}
