/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
