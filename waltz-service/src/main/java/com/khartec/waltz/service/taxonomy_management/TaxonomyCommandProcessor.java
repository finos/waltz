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

package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangePreview;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public interface TaxonomyCommandProcessor {

    Logger LOG = LoggerFactory.getLogger(TaxonomyCommandProcessor.class);


    default void checkDomain(TaxonomyChangeCommand cmd, EntityKind expectedDomain) {
        Checks.checkTrue(
                cmd.changeDomain().kind() == expectedDomain,
                "Incorrect domain, expected [%s] got [%s]",
                expectedDomain,
                cmd.changeDomain().kind());
    }


    default <T> boolean hasNoChange(T currentValue, T newValue, String fieldName) {
        if (currentValue.equals(newValue)) {
            LOG.info("Command will have no effect, '{}' is already '{}'", fieldName, newValue);
            return true;
        } else {
            return false;
        }
    }


    TaxonomyChangePreview preview(TaxonomyChangeCommand cmd);
    TaxonomyChangeCommand apply(TaxonomyChangeCommand command, String userId);

    Set<TaxonomyChangeType> supportedTypes();
    EntityKind domain();


    default void doBasicValidation(TaxonomyChangeCommand cmd) {
        cmd.validate();
        checkDomain(cmd, domain());
        Checks.checkTrue(
                supportedTypes().contains(cmd.changeType()),
                "Incorrect type, expected [%s] got [%s]",
                supportedTypes(),
                cmd.changeType());  }

}
