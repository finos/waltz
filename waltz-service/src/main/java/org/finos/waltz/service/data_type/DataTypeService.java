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

package org.finos.waltz.service.data_type;

import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.data_type.search.DataTypeSearchDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.head;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class DataTypeService {

    private static final LogicalFlowIdSelectorFactory LOGICAL_FLOW_ID_SELECTOR_FACTORY = new LogicalFlowIdSelectorFactory();

    private final DataTypeDao dataTypeDao;
    private final DataTypeSearchDao searchDao;
    private final LogicalFlowDao logicalFlowDao;


    @Autowired
    public DataTypeService(DataTypeDao dataTypeDao,
                           DataTypeSearchDao searchDao,
                           LogicalFlowDao logicalFlowDao) {
        checkNotNull(dataTypeDao, "dataTypeDao must not be null");
        checkNotNull(searchDao, "searchDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowService cannot be null");

        this.dataTypeDao = dataTypeDao;
        this.searchDao = searchDao;
        this.logicalFlowDao = logicalFlowDao;
    }


    public List<DataType> findAll() {
        return dataTypeDao.findAll();
    }


    public DataType getDataTypeById(long dataTypeId) {
        return dataTypeDao.getById(dataTypeId);
    }


    public DataType getDataTypeByCode(String code) {
        return dataTypeDao.getByCode(code);
    }

    public List<EntityReference> findByIdSelector(Select<Record1<Long>> selector) {
        return dataTypeDao.findByIdSelectorAsEntityReference(selector);
    }


    public Collection<DataType> search(EntitySearchOptions options) {
        return searchDao.search(options);
    }

    /**
     * Attempts to return the datatype that has been declared as unknown (if one exists)
     * @return `Optional.of(unknownDataType)` if an unknown datatype has been defined otherwise `Optional.empty()`.
     */
    public Optional<DataType> getUnknownDataType() {
        return dataTypeDao
                .findAll()
                .stream()
                .filter(DataType::unknown)
                .findFirst();
    }


    public Set<DataType> findSuggestedByEntityRef(EntityReference entityReference) {
        Set<EntityKind> directlySupportedKinds = asSet(
                EntityKind.APPLICATION,
                EntityKind.ACTOR);

        Set<EntityKind> indirectlySupportedKinds = asSet(
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.PHYSICAL_SPECIFICATION);

        if (directlySupportedKinds.contains(entityReference.kind())) {
            return dataTypeDao.findSuggestedByEntityRef(entityReference);
        } else if (indirectlySupportedKinds.contains(entityReference.kind())) {
            // if we are dealing with a flow or a spec, find the associated source and use it's datatypes
            Select<Record1<Long>> selector = LOGICAL_FLOW_ID_SELECTOR_FACTORY.apply(mkOpts(entityReference));
            return head(logicalFlowDao.findBySelector(selector))
                    .map(f -> dataTypeDao.findSuggestedByEntityRef(f.source()))
                    .orElse(Collections.emptySet());
        } else {
            throw new UnsupportedOperationException(format("Cannot find suggested data types for entity kind: %s", entityReference.kind()));
        }
    }

}
