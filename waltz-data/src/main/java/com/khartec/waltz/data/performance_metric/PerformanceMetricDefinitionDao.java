package com.khartec.waltz.data.performance_metric;

import com.khartec.waltz.model.performance_metric.ImmutablePerformanceMetricDefinition;
import com.khartec.waltz.model.performance_metric.PerformanceMetricDefinition;
import com.khartec.waltz.schema.tables.records.PerformanceMetricDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PerformanceMetricDefinition.PERFORMANCE_METRIC_DEFINITION;

@Repository
public class PerformanceMetricDefinitionDao {

    private static final RecordMapper<? super Record, PerformanceMetricDefinition> TO_DOMAIN_MAPPER = r -> {
        PerformanceMetricDefinitionRecord record = r.into(PERFORMANCE_METRIC_DEFINITION);
        return ImmutablePerformanceMetricDefinition.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .categoryName(record.getCategoryName())
                .categoryDescription(record.getCategoryDescription())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PerformanceMetricDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<PerformanceMetricDefinition> findAll() {
        return dsl
                .select(PERFORMANCE_METRIC_DEFINITION.fields())
                .from(PERFORMANCE_METRIC_DEFINITION)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public PerformanceMetricDefinition getById(long id) {
        return dsl
                .select(PERFORMANCE_METRIC_DEFINITION.fields())
                .from(PERFORMANCE_METRIC_DEFINITION)
                .where(PERFORMANCE_METRIC_DEFINITION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }
}
