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
