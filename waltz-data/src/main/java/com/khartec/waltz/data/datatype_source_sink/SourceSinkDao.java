package com.khartec.waltz.data.datatype_source_sink;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.datatype_source_sink.DataTypeSourceSinks;
import com.khartec.waltz.model.datatype_source_sink.ImmutableDataTypeSourceSinks;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.schema.tables.DataType;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;

@Repository
public class SourceSinkDao {

    private final DSLContext dsl;
    private final DataFlow df = DATA_FLOW.as("df");

    private final DataType dt = DATA_TYPE.as("dt");
    private final Application app = APPLICATION.as("app");
    private final AuthoritativeSource au = AuthoritativeSource.AUTHORITATIVE_SOURCE.as("au");
    private final EntityHierarchy eh = EntityHierarchy.ENTITY_HIERARCHY.as("eh");
    private final OrganisationalUnit ou = OrganisationalUnit.ORGANISATIONAL_UNIT.as("ou");
    private final Condition bothApps =
            df.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                    .and(df.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));


    private RecordMapper<? super Record10<Long, String, String, Long, String, String, Long, String, String, Integer>,
            DataTypeSourceSinks> TO_SOURCE_SINK_MAPPER = record -> {

        ImmutableEntityReference source = ImmutableEntityReference.builder()
                .id(record.value1())
                .kind(EntityKind.valueOf(record.value2()))
                .name(record.value3())
                .build();

        ImmutableEntityReference dataType = ImmutableEntityReference.builder()
                .id(record.value4())
                .kind(EntityKind.DATA_TYPE)
                .name(record.value5())
                .build();

        ImmutableEntityReference orgUnit = ImmutableEntityReference.builder()
                .id(record.value7())
                .kind(EntityKind.valueOf(record.value8()))
                .name(record.value9())
                .build();

        return ImmutableDataTypeSourceSinks.builder()
                .source(source)
                .dataType(dataType)
                .rating(Rating.valueOf(record.value6()))
                .organisationalUnit(orgUnit)
                .sinkCount(record.value10())
                .build();
    };


    @Autowired
    public SourceSinkDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<DataTypeSourceSinks> findByIdSelector(Select<Record1<Long>> dataTypeIdSelector) {

        final Application sapp = APPLICATION.as("sapp");

        return findRatedSourcesAndSinks(dataTypeIdSelector, sapp);
    }


    private List<DataTypeSourceSinks> findRatedSourcesAndSinks(Select<Record1<Long>> dataTypeIdSelector, Application sapp) {
        return dsl
                .select(df.SOURCE_ENTITY_ID,
                        df.SOURCE_ENTITY_KIND,
                        sapp.NAME,
                        dt.ID,
                        dt.CODE,
                        au.RATING,
                        au.PARENT_ID,
                        au.PARENT_KIND,
                        ou.NAME,
                        DSL.count(df.TARGET_ENTITY_ID).as("sink_count"))
                .from(df)
                .innerJoin(app)
                .on(app.ID.eq(df.TARGET_ENTITY_ID))
                .innerJoin(sapp)
                .on(sapp.ID.eq(df.SOURCE_ENTITY_ID))
                .innerJoin(au)
                .on(au.APPLICATION_ID.eq(df.SOURCE_ENTITY_ID)
                        .and(df.DATA_TYPE.eq(au.DATA_TYPE)))
                .and(au.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()))
                .innerJoin(eh)
                .on(eh.ANCESTOR_ID.eq(au.PARENT_ID))
                .and(eh.KIND.eq(EntityKind.ORG_UNIT.name()))
                .and(eh.ID.eq(app.ORGANISATIONAL_UNIT_ID))
                .innerJoin(ou)
                .on(ou.ID.eq(au.PARENT_ID))
                .innerJoin(dt)
                .on(dt.CODE.eq(df.DATA_TYPE))
                .where(bothApps)
                .and(dt.ID.in(dataTypeIdSelector))
                .groupBy(df.SOURCE_ENTITY_ID,
                        df.SOURCE_ENTITY_KIND,
                        sapp.NAME,
                        dt.ID,
                        dt.CODE,
                        au.RATING,
                        au.PARENT_ID,
                        au.PARENT_KIND,
                        ou.NAME)
                .fetch()
                .map(TO_SOURCE_SINK_MAPPER);
    }

}
