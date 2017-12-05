

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import _ from 'lodash';
import RowGroup from "./row-group";


const DEFAULT_OPTIONS = {
    xId: null,
    yId: null,
};


export default class DrillGrid {

    constructor(xAxis,
                yAxis,
                options) {

        this.options = Object.assign({}, DEFAULT_OPTIONS, options);

        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.rowGroups = [];
        this.listeners = [];
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

        this._focusCol(this.options.xId, false);
        this._focusRowGroup(this.options.yId, false);

        this.rowGroups = _.map(
            this.yAxis.current.domain,
            yDatum => new RowGroup(yDatum, this.xAxis));
        this.notifyListeners();

        return this;
    }


    hasRowGroups() {
        return (this.rowGroups || []).length > 0
    }


    _focusRowGroup(id) {
        this.yAxis.focus(id);
        return this;
    }


    _focusCol(id) {
        this.xAxis.focus(id);
        return this;
    }

}
