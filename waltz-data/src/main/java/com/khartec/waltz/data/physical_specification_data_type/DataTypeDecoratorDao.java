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

package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import org.jooq.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class DataTypeDecoratorDao {

    public abstract DataTypeDecorator getByEntityIdAndDataTypeId(long entityId, long dataTypeId);

    public abstract List<DataTypeDecorator> findByEntityId(long entityId);

    public abstract List<DataTypeDecorator> findByEntityIdSelector(Select<Record1<Long>> idSelector,
                                                                   Optional<EntityKind> entityKind);

    public abstract List<DataTypeDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector);
    public abstract List<DataTypeDecorator> findByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector);

    //only implemented for logical flows
    @Deprecated
    public abstract Collection<DataTypeDecorator> findByFlowIds(List<Long> flowIds);

    public abstract int[] addDecorators(Collection<DataTypeDecorator> dataTypeDecorators);

    public abstract int[] removeDataTypes(Collection<DataTypeDecorator> dataTypeDecorators);

//might need to remove it and replace it with on demand functionality
 /*   public int rippleDataTypesToLogicalFlows() {
        return dsl.insertInto(LOGICAL_FLOW_DECORATOR)
                .select(DSL
                        .selectDistinct(
                                PHYSICAL_FLOW.LOGICAL_FLOW_ID,
                                DSL.val(EntityKind.DATA_TYPE.name()),
                                PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID,
                                DSL.val(AuthoritativenessRating.NO_OPINION.name()),
                                DSL.val("waltz"),
                                DSL.val(Timestamp.valueOf(nowUtc())),
                                DSL.val("admin"))
                        .from(PHYSICAL_SPEC_DATA_TYPE)
                        .join(PHYSICAL_FLOW).on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                        .leftJoin(LOGICAL_FLOW_DECORATOR)
                            .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID)
                                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID)))
                        .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.isNull()))
                .execute();
    }*/

}
