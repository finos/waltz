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

package org.finos.waltz.model.proposed_flow;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ProposedFlowWorkflowState {

    PROPOSED_CREATE("Proposed Create", false),
    PENDING_APPROVALS("Pending Approval", false),
    SOURCE_APPROVED("Source Approved", false),
    TARGET_APPROVED("Target Approved", false),
    FULLY_APPROVED("Fully Approved", true),
    SOURCE_REJECTED("Source Rejected", true),
    TARGET_REJECTED("Target Rejected", true),
    CANCELLED("Cancelled", true);

    private final String prettyName;
    private final boolean endState;

    ProposedFlowWorkflowState(String prettyName, boolean endState) {

        this.prettyName = prettyName;
        this.endState = endState;
    }

    public String prettyName() {
        return prettyName;
    }
    public boolean isEndState(){ return  endState; }

    public static List<ProposedFlowWorkflowState> getEndStates() {
        return Arrays.stream(values())
                .filter(ProposedFlowWorkflowState::isEndState)
                .collect(Collectors.toList());
    }
}
