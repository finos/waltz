/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
