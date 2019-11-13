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


import {registerStores} from "../common/module-utils";
import * as TagStore from "./services/tag-store";
import Routes from "./routes";
import TagList from "./components/tag-list";
import TagEdit from "./components/tag-edit";
import AllTags from "./components/all-tags";


export default () => {

    const module = angular.module("waltz.tags", []);

    module
        .config(Routes);

    module
        .component("waltzTagList",  TagList )
        .component("waltzTagEdit",  TagEdit )
        .component("waltzAllTags",  AllTags);

    registerStores(module, [TagStore]);

    return module.name;
};
