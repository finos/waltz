
/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const row = {
    height: 40,
    label: {
        width: 150
    }
};

const rowGroup = {
    emptyHeight: 40,
    padding: 0.2,
    label: {
        width: 150,
        dy: 20,
        dx: 20
    }
};

const column = {
    width: 100,
    height: 120,
    padding: 0.2,
    dx: rowGroup.label.width + row.label.width,
    label: {
        dx: 20,
        height: 100,
        angle: -45
    }
};

export const ROADMAP_LAYOUT_OPTIONS = {
    column,
    row,
    rowGroup
};
