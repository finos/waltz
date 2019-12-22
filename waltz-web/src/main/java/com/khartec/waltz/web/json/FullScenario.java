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

package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioAxisItem;
import com.khartec.waltz.model.scenario.ScenarioRatingItem;
import org.immutables.value.Value;

import java.util.Collection;

@Value.Immutable
@JsonSerialize(as = ImmutableFullScenario.class)
@JsonDeserialize(as = ImmutableFullScenario.class)
public abstract class FullScenario {

    public abstract Roadmap roadmap();
    public abstract Scenario scenario();
    public abstract Collection<ScenarioAxisItem> axisDefinitions();
    public abstract Collection<ScenarioRatingItem> ratings();

}
