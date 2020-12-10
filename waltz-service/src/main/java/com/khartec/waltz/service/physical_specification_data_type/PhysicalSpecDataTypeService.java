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

package com.khartec.waltz.service.physical_specification_data_type;

import com.khartec.waltz.data.datatype_decorator.PhysicalSpecDecoratorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalSpecDataTypeService {

    private final PhysicalSpecDecoratorDao physicalSpecDecoratorDao;


    @Autowired
    public PhysicalSpecDataTypeService(PhysicalSpecDecoratorDao physicalSpecDecoratorDao) {
        checkNotNull(physicalSpecDecoratorDao, "physicalSpecDataTypeDecoratorDao cannot be null");
        this.physicalSpecDecoratorDao = physicalSpecDecoratorDao;
    }

    public int rippleDataTypesToLogicalFlows() {
        return physicalSpecDecoratorDao.rippleDataTypesToLogicalFlows();
    }


}
