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

const RELATES_TO = { code: 'RELATES_TO', label: 'Relates To' };
const IS_DEPRECATED_BY = { code: 'DEPRECATES', label: 'Is Deprecated By' };
const DEPRECATES = { code: 'DEPRECATES', label: 'Deprecates' };

export const availableRelationshipKinds = {
    'APP_GROUP-APP_GROUP': [RELATES_TO],
    'APP_GROUP-MEASURABLE': [RELATES_TO],
    'APP_GROUP-CHANGE_INITIATIVE': [RELATES_TO],
    'MEASURABLE-APP_GROUP': [RELATES_TO],
    'MEASURABLE-CHANGE_INITIATIVE': [RELATES_TO, IS_DEPRECATED_BY],
    'MEASURABLE-MEASURABLE': [RELATES_TO],
    'CHANGE_INITIATIVE-APP_GROUP': [RELATES_TO],
    'CHANGE_INITIATIVE-MEASURABLE': [RELATES_TO, DEPRECATES],
    'CHANGE_INITIATIVE-CHANGE_INITIATIVE': [RELATES_TO]
};
