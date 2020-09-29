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

import { select } from "d3-selection";
import { arc, pie } from "d3-shape";
import { easeElasticOut } from "d3-ease";
import "d3-selection-multi";

import _ from "lodash";
import { isPieEmpty } from "./pie-utils";
import template from "./pie.html";


/**
 * data: [....]
 *
 * config: {
 *   colorProvider,
 *   valueProvider : d => d.count,
 *   labelProvider : d => ''
 * }
 */

const bindings = {
    data: "<",
    config: "<",
    selectedSegmentKey: "<"
};


const styles = {
    emptyPie: "empty-pie"
};

const defaultOnSelect = (d) => console.log("pie.onSelect default handler: ", d);


const DEFAULT_SIZE = 80;
const FOCUS_DURATION = 1000;
const BLUR_DURATION = 50;


function mkPieArc(radius, focused = false) {
    if(focused) {
        return arc()
            .outerRadius(radius - 1)
            .innerRadius(radius / 2.5)
            .padAngle(0.07)
            .cornerRadius(0);
    } else {
        return arc()
            .outerRadius(radius - 3)
            .innerRadius(radius / 2.5)
            .padAngle(0.07)
            .cornerRadius(0);
    }
}

const expandArc = (selection, radius) => {
    const arcPath = select(selection);
    const pieArc = mkPieArc(radius, true);
    arcPath
        .transition()
        .ease(easeElasticOut.amplitude(2))
        .duration(FOCUS_DURATION)
        .attr("d", d => pieArc(d));
};

const unexpandArc = (selection, radius) => {
    const arcPath = select(selection);
    const pieArc = mkPieArc(radius, false);
    arcPath
        .transition()
        .duration(BLUR_DURATION)
        .attr("d", d => pieArc(d));
};


function renderArcs(holder, config, data, onSelect) {
    const {
        colorProvider,
        valueProvider = (d) => d.count,
        idProvider = (d) => d.data.key,
        size = DEFAULT_SIZE
    } = config;

    const radius = size / 2;

    const pieArc = mkPieArc(radius, false);

    const pieLayout = pie()
        .value(valueProvider);

    const pieData = pieLayout(_.filter(data, r => r.count > 0));

    const arcs = holder
        .selectAll(".arc")
        .data(pieData, d => idProvider(d));

    const newArcs = arcs
        .enter()
        .append("path")
        .classed("arc clickable", true)
        .on("click", d => onSelect(d.data));

    newArcs
        .append("title")
        .text(d => `${d.data.key} - #${d.data.count}`);

    arcs
        .merge(newArcs)
        .attr("d", d => pieArc(d))
        .attr("fill", d => colorProvider(d.data).brighter())
        .attr("stroke", d => colorProvider(d.data));

    arcs.exit()
        .remove();

    const emptyPie = holder
        .selectAll(`.${styles.emptyPie}`)
        .data(isPieEmpty(data) ? [1] : []);

    emptyPie
        .enter()
        .append("circle")
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("r", radius / 2)
        .attr("fill", "#eee")
        .attr("stroke", "#bbb")
        .attr("stroke-dasharray", [5, 1])
        .classed(styles.emptyPie, true);

    emptyPie
        .exit()
        .remove();
}


function render(svg, config, data, onSelect) {
    const { size = DEFAULT_SIZE } = config;
    const width = size;
    const height = size;

    const mainGroup = svg
        .attr("width", width)
        .attr("height", height)
        .selectAll(".main-group")
        .data([1]);

    const newMainGroup = mainGroup
        .enter()
        .append("g")
        .classed("main-group", true);

    const g = newMainGroup
        .merge(mainGroup)
        .attr("transform", `translate(${width / 2},${height / 2})`);

    renderArcs(g, config, data, onSelect);
}


function controller($element, $scope) {
    const vizElem = $element[0].querySelector(".waltz-pie");

    const svg = select(vizElem)
        .append("svg");

    const vm = this;

    vm.$onChanges = (changes) => {
        if (vm.data && vm.config && changes.data) {
            const onSelectFn = vm.config.onSelect || defaultOnSelect;
            const onSelect = (d) => $scope.$apply(() => onSelectFn(d));
            render(svg, vm.config, vm.data, onSelect);
        }

        if (changes.selectedSegmentKey) {
            svg.selectAll(".arc")
                .classed("wp-selected", function(d) {
                    const isSelected = d.data.key === vm.selectedSegmentKey;
                    if(isSelected) {
                        expandArc(this, vm.config.size / 2);
                    } else {
                        unexpandArc(this, vm.config.size / 2);
                    }
                    return isSelected;
                });
        }
    };
}


controller.$inject = [
    "$element",
    "$scope"
];


const component = {
    bindings,
    controller,
    template
};


export default component;
