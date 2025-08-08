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

package org.finos.waltz.service.workflow_state_machine.proposed_flow;

public enum ProposedFlowWorkflowState {

    PROPOSED_CREATE("Proposed Create"),
    PENDING_APPROVALS("Pending Approval"),
    SOURCE_APPROVED("Source Approved"),
    TARGET_APPROVED("Target Approved"),
    FULLY_APPROVED("Fully Approved"),
    SOURCE_REJECTED("Source Rejected"),
    TARGET_REJECTED("Target Rejected");

    private final String prettyName;

    ProposedFlowWorkflowState(String prettyName) {
        this.prettyName = prettyName;
    }

    public String prettyName() {
        return prettyName;
    }
}
