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
import _ from "lodash";
import RowGroup from "./row-group";


const DEFAULT_OPTIONS = {
    xId: null,
    yId: null,
};


export default class DrillGrid {

    constructor(definition,
                xAxis,
                yAxis,
                options) {

        this.options = Object.assign({}, DEFAULT_OPTIONS, options);

        this.definition = definition;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.rowGroups = [];
        this.listeners = [];
        this.refresh();
    }


    addListener(callback) {
        this.listeners.push(callback);
        callback(this);
    }


    notifyListeners() {
        _.each(this.listeners, cb => cb(this));
    }


    refresh(newOptions) {
        _.merge(this.options, newOptions);

        this.yAxis.focus(this.options.yId);
        this.xAxis.focus(this.options.xId);

        this.rowGroups = _
            .chain(this.yAxis.current.domain)
            .map(yDatum => new RowGroup(yDatum, this.xAxis, this.options.focusApp))
            .reject(rg => rg.isEmpty())
            .value();

        this.restrictXDomainToUsedValues();
        this.notifyListeners();

        return this;
    }

    restrictXDomainToUsedValues() {
        const activeXIds = _
            .chain(this.rowGroups)
            .flatMap(rg => rg.rows)
            .flatMap(r => r.mappings)
            .filter(r => r.rating != 'Z')
            .map(r => r.colId)
            .uniq()
            .value();

        this.xAxis.current.domain = _.filter(
            this.xAxis.current.domain,
            d => _.includes(activeXIds, d.id));
    }


    isEmpty() {
        return (this.rowGroups || []).length == 0
    }

}
