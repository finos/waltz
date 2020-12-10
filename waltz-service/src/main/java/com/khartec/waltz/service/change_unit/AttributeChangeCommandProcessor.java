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

package com.khartec.waltz.service.change_unit;

import com.khartec.waltz.model.attribute_change.AttributeChange;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.khartec.waltz.common.Checks.*;

public interface AttributeChangeCommandProcessor {
    Logger LOG = LoggerFactory.getLogger(AttributeChangeCommandProcessor.class);


    default void doBasicValidation(AttributeChange attributeChange, ChangeUnit changeUnit, String userName) {
        checkNotNull(attributeChange, "attributeChange cannot be null");
        checkNotNull(changeUnit, "changeUnit cannot be null");
        checkNotEmpty(userName, "userName cannot be null or empty");
        checkTrue(attributeChange.name().equals(this.supportedAttribute()),
                "Attribute name does not match attribute support by command");
    }


    String supportedAttribute();


    boolean apply(AttributeChange attributeChange,
                  ChangeUnit changeUnit,
                  String userName);
}
