/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import _ from "lodash";
import {sameRef} from "../../client/common/entity-utils";

function isMovedToItself(cmd) {
    return cmd.changeType === "MOVE"
        && cmd.primaryReference.id === cmd.params.destinationId;
}

function isMovedToAnExistingChild(cmd, measurable) {
    return cmd.changeType === "MOVE"
        && _.some(measurable.children,
            c => (c.id === cmd.params.destinationId));
}

function isSameChangeTypeCommandAlreadyExist(cmd, pendingChanges) {
    return _.some(pendingChanges,
        c => c.changeType === cmd.changeType
            && sameRef(c.primaryReference, cmd.primaryReference));
}

export function getValidationErrorIfMeasurableChangeIsNotValid(cmd, measurable, pendingChanges) {
    const errorIsNodeMovedToItSelf =
        "Cannot set a Measurable as its own parent, ignoring....";
    const errorIsNodeMovedToAnExistingChild =
        "Cannot add a measurable as parent if it is already a child of the subject, ignoring....";
    const ErrorIsSameChangeTypeExistForTheNode =
        `${cmd.changeType} is already pending for ${cmd.primaryReference.name}, Please delete the pending request to recreate`;

    return isMovedToItself(cmd)
        ? errorIsNodeMovedToItSelf
        : isMovedToAnExistingChild(cmd, measurable)
            ? errorIsNodeMovedToAnExistingChild
            : isSameChangeTypeCommandAlreadyExist(cmd, pendingChanges)
                ? ErrorIsSameChangeTypeExistForTheNode
                : null;
}

