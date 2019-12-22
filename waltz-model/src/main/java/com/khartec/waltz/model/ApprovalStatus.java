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

package com.khartec.waltz.model;

public enum ApprovalStatus {

    PENDING_APPROVAL,   // Pending approval, therefore no current opinion
    APPROVED_ALL,       // Approved for internal and external use
    INTERNAL_ONLY,      // Approved for internal use only, external use would require further review
    REJECTED_ALL,       // Cannot be used
    REQUIRES_EXCEPTION  // Exception must be obtained before use

}
