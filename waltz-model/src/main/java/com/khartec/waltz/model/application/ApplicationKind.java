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

package com.khartec.waltz.model.application;

public enum ApplicationKind {
    /** Applications which have been developed by in-house software teams */
    IN_HOUSE,

    /** Deprecated, use THIRD_PARTY or CUSTOMISED instead **/
    @Deprecated
    INTERNALLY_HOSTED,

    /** Externally hosted applications such as Salesforce **/
    EXTERNALLY_HOSTED,

    /** End user computing - Applications not owned by IT **/
    EUC,

    /** Third party applications which have not been customised **/
    THIRD_PARTY,

    /** Third party applications which have been customised **/
    CUSTOMISED,

    /** Applications which are not owned by the organisation **/
    EXTERNAL
}
