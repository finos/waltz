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
import angular from "angular";

import {registerComponents, registerStore} from "../common/module-utils";
import * as EntitySearchStore from "./services/entity-search-store";
import EntityLinkList from "./components/entity-link-list/entity-link-list";

import entityHierarchyNavigator from "./components/entity-hierarchy-navigator/entity-hierarchy-navigator";
import entityInvolvementEditor from "./components/entity-involvement-editor/entity-involvement-editor";
import entitySelector from "./components/entity-selector/entity-selector";
import immediateHierarchyNavigator from "./components/immediate-hierarchy-navigator/immediate-hierarchy-navigator";
import relatedEntityEditor from "./components/related-entity-editor/related-entity-editor";


export default () => {
    const module = angular.module('waltz.entity', []);

    registerStore(module, EntitySearchStore);

    module
        .component('waltzEntityHierarchyNavigator', entityHierarchyNavigator)
        .component('waltzEntityInvolvementEditor', entityInvolvementEditor)
        .component('waltzEntitySelector', entitySelector)
        .component('waltzImmediateHierarchyNavigator', immediateHierarchyNavigator)
        .component('waltzRelatedEntityEditor', relatedEntityEditor);

    registerComponents(module, [ EntityLinkList ]);

    return module.name;
};
