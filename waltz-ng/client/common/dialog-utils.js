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
 * Prompts the user with an ok/cancel box.
 * If user selects 'ok' the `okayFn` will be executed.
 * If user selects `cancel` the (optional) `cancelFn` will be executed.
 *
 * @param prompt - text to use in prompt
 * @param okayFn
 * @param cancelFn
 * @returns {*} - result of okFn or cancelFn
 */
export function confirmWithUser(prompt,
                         okayFn,
                         cancelFn = () => {}) {
    return confirm(prompt)
        ? okayFn()
        : cancelFn();
}
