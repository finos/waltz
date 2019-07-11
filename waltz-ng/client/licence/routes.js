/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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

import LicenceList from "./pages/list/licence-list";
import LicenceView from "./pages/view/licence-view";


const baseState = {
};


const viewState = {
    url: "licence/{id:int}",
    views: { "content@": LicenceView.id }
};

const listState = {
    url: "licence",
    views: {
        "content@": LicenceList.id
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.licence", baseState)
        .state("main.licence.list", listState)
        .state("main.licence.view", viewState);
}


setup.$inject = ["$stateProvider"];


export default setup;