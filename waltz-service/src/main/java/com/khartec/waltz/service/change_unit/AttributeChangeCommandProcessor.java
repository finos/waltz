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

import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.change_unit.UpdateExecutionStatusCommand;
import com.khartec.waltz.model.command.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.khartec.waltz.common.Checks.*;

public interface AttributeChangeCommandProcessor {
    Logger LOG = LoggerFactory.getLogger(AttributeChangeCommandProcessor.class);


    default <T> boolean hasNoChange(T currentValue, T newValue, String fieldName) {
        if (currentValue.equals(newValue)) {
            LOG.info("Command will have no effect, '{}' is already '{}'", fieldName, newValue);
            return true;
        } else {
            return false;
        }
    }


    default void doBasicValidation(UpdateExecutionStatusCommand command, ChangeUnit changeUnit, String userName) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(changeUnit, "changeUnit cannot be null");
        checkNotEmpty(userName, "userName cannot be null or empty");
        checkTrue(changeUnit.executionStatus().equals(command.executionStatus().oldVal()),
                "changeUnits execution status does not match old status in command");
    }


    String supportedAttribute();

    CommandResponse<UpdateExecutionStatusCommand> apply(UpdateExecutionStatusCommand command,
                                                        ChangeUnit changeUnit,
                                                        String userName);
}
