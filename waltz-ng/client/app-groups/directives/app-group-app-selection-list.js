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

/**
 * This directive renders an application list which exposes simple controls for handling
 * selections (callback on 'add'), and handling refocuses.  The 'isMember' callback is
 * used to strikethrough text.
 */

import template from './app-group-app-selection-list.html';

const BINDINGS = {
    applications: '=', // [app]
    isMember: '=', // function
    refocus: '=',  // function
    add: '='  // function
};


function controller() {

}

controller.$inject = [];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template,
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};