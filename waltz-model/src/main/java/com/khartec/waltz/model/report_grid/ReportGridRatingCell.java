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

package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.CommentProvider;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridRatingCell.class)
@JsonDeserialize(as = ImmutableReportGridRatingCell.class)
public abstract class ReportGridRatingCell implements CommentProvider {

    public abstract EntityKind columnEntityKind();  // x
    public abstract long columnEntityId();  // x

    public abstract long applicationId(); // y
    public abstract long ratingId();

}