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

import {remote} from "./remote";
import {checkIsFlowClassificationRuleCreateCommand, checkIsFlowClassificationRuleUpdateCommand} from "../common/checks";

export function mkFlowClassificationRuleStore() {

    const update = (cmd) => {
        checkIsFlowClassificationRuleUpdateCommand(cmd);
        return remote
            .execute("PUT","api/flow-classification-rule", cmd)
    }

    const findAll = () => {
        return remote
            .execute("GET", "api/flow-classification-rule")
    }

    const getById = (id) => {
        return remote
            .fetchViewData("GET", `api/flow-classification-rule/id/${id}`);
    }

    const create = (cmd) => {
        checkIsFlowClassificationRuleCreateCommand(cmd);
        return remote.execute("POST", "api/flow-classification-rule", cmd)
    }

    const findCompanionAppRulesById = (id, force = false) => {
        return remote
            .fetchViewList("GET", `api/flow-classification-rule/companion-rules/entity/id/${id}`, null, {force})
    }

    const findCompanionDataTypeRulesById = (id, force = false) => {
        return remote
            .fetchViewList("GET", `api/flow-classification-rule/companion-rules/data-type/id/${id}`, null, {force})
    }

    return {
        findAll,
        create,
        update,
        getById,
        findCompanionAppRulesById,
        findCompanionDataTypeRulesById
    };
}

export const flowClassificationRuleStore = mkFlowClassificationRuleStore();
