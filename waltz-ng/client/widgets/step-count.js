/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

export default () => ({
    restrict: 'E',
    replace: true,
    template: '<svg width="50" height="50">\n    <circle r="25" cx="25" cy="25" fill="{{colour}}"></circle>\n    <text text-anchor="middle" x="25" y="32" font-size="24">\n        {{count}}\n    </text>\n</svg>',
    scope: {
        count: '@',
        colour: '@'
    }
});
