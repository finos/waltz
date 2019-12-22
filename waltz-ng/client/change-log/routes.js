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

import ChangeLogView from "./view";


const baseState = {
    url: 'change-log'
};


const entityViewState = {
    url: '/view/entity/{kind:string}/{id:int}?name',
    views: { 'content@': ChangeLogView }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.change-log', baseState)
        .state('main.change-log.view', entityViewState);
}


setup.$inject = ['$stateProvider'];


export default setup;