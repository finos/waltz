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

package org.finos.waltz.model.logical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;


@Value.Immutable
@JsonSerialize(as = ImmutableLogicalFlowView.class)
@JsonDeserialize(as = ImmutableLogicalFlowView.class)
public abstract class LogicalFlowView {

    public abstract Set<LogicalFlow> logicalFlows();

    public abstract Set<PhysicalFlow> physicalFlows();

    public abstract Set<PhysicalSpecification> physicalSpecifications();

    public abstract Set<RatingSchemeItem> ratingSchemeItems();

    public abstract Set<DataTypeDecorator> logicalFlowDataTypeDecorators();

    public abstract Set<DataTypeDecorator> physicalSpecificationDataTypeDecorators();

    public abstract Set<AssessmentDefinition> logicalFlowAssessmentDefinitions();

    public abstract Set<AssessmentDefinition> physicalFlowAssessmentDefinitions();

    public abstract Set<AssessmentDefinition> physicalSpecificationAssessmentDefinitions();

    public abstract Set<AssessmentRating> logicalFlowRatings();

    public abstract Set<AssessmentRating> physicalFlowRatings();

    public abstract Set<AssessmentRating> physicalSpecificationRatings();

}
