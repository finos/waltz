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

export default (module) => {
    module
        .component('waltzBucketChart', require('./bucket-chart'))
        .component('waltzMultiSelectTreeControl', require('./multi-select-tree-control/multi-select-treecontrol'))
        .component('waltzSearchControl', require('./search-control/search-control'))
        .component('waltzEntityIconLabel', require('./entity-link/entity-icon-label'))
        .component('waltzEntityLink', require('./entity-link/entity-link'))
        .component('waltzGrid', require('./grid/grid'))
        .component('waltzGridWithSearch', require('./grid/grid-with-search'));

};
