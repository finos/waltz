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

package org.finos.waltz.data.datatype_decorator;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.jooq.Record1;
import org.jooq.Select;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class DataTypeDecoratorDao {

    public abstract DataTypeDecorator getByEntityIdAndDataTypeId(long entityId, long dataTypeId);

    public abstract List<DataTypeDecorator> findByEntityId(long entityId);

    public abstract List<DataTypeDecorator> findByEntityIdSelector(Select<Record1<Long>> idSelector,
                                                                   Optional<EntityKind> entityKind);

    public abstract List<DataTypeDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector);

    public abstract List<DataTypeDecorator> findByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector);

    //only implemented for logical flows
    public abstract Set<DataTypeDecorator> findByFlowIds(Collection<Long> flowIds);

    public abstract int[] addDecorators(Collection<DataTypeDecorator> dataTypeDecorators);

    public abstract int removeDataTypes(EntityReference associatedEntityRef, Collection<Long> dataTypeIds);

    public abstract List<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristics(EntityReference ref);

    public abstract Set<DataTypeDecorator> findByLogicalFlowIdSelector(Select<Record1<Long>> flowIdSelector);
}
