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

import {select} from "d3-selection";
import "d3-selection-multi";
import { authoritativeSourceColorScale } from "../../common/colors";


const radius = 8;
const padding = 3;


function link(scope, elem) {

    const data = ["PRIMARY", "SECONDARY"];
    const svg = select(elem[0]);

    svg.selectAll("circle")
        .data(data)
        .enter()
        .append("circle")
        .attr("cx", (d, i) => i * (radius * 2 + padding * 2) + radius + padding / 2)
        .attr("cy", radius + padding / 2)
        .attr("r", radius);

    scope.$watch("value", (value) => svg
        .selectAll("circle")
        .data(data)
        .attr("fill", (d) => ( d === value) ? authoritativeSourceColorScale(d) : "#eee")
        .attr("stroke", (d) => ( d === value) ? authoritativeSourceColorScale(d).darker() : "#ddd"));
}


export default () => ({
    restrict: "E",
    replace: true,
    template: `<svg width="${ 3 * ( radius * 2 + padding * 2)}" height="${radius * 2 + padding}"></svg>`,
    scope: { value: "@"},
    link
});
