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

function isDuplicateAction(c, cmd) {
    if (c.changeType === cmd.changeType
        && sameRef(c.primaryReference, cmd.primaryReference)) {
        const allowedChangeTypes = ["ADD_PEER", "ADD_CHILD"];
        return !(allowedChangeTypes.includes(cmd.changeType)
            && cmd.params.name !== c.params.name);
    } else {
        return false;
    }
}


function isSameChangeTypeCommandAlreadyExist(cmd, pendingChanges) {
    return _.some(pendingChanges, c => isDuplicateAction(c, cmd));
}


export function getValidationErrorIfMeasurableChangeIsNotValid(cmd, measurable, pendingChanges) {
    const errorIsNodeMovedToItSelf =
        "Cannot set a Measurable as its own parent, ignoring....";
    const errorIsNodeMovedToAnExistingChild =
        "Cannot add a measurable as parent if it is already a child of the subject, ignoring....";
    const errorIsSameChangeTypeExistForTheNode =
        `${cmd.changeType} is already pending for ${cmd.primaryReference.name}, Please delete the pending request to recreate`;

    if (isMovedToItself(cmd)){
        return errorIsNodeMovedToItSelf;
    } else if (isMovedToAnExistingChild(cmd, measurable)) {
        return errorIsNodeMovedToAnExistingChild;
    } else if (isSameChangeTypeCommandAlreadyExist(cmd, pendingChanges)) {
        return errorIsSameChangeTypeExistForTheNode;
    } else {
        return null;
    }
}

