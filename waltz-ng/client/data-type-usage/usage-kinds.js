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

const usageKinds = [
    {
        displayName: 'Consumer',
        kind: "CONSUMER",
        nameDescription: "A consumer is an application that acquires data from an upstream source",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>incoming</strong> flows'
    }, {
        displayName: 'Distributor',
        kind: "DISTRIBUTOR",
        nameDescription: "A distributor is an application that provides data to one or more downstream applications",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows'
    }, {
        displayName: 'Modifier',
        kind: "MODIFIER",
        nameDescription: "A modifier is an application that changes the data acquired from an upstream source. Changes may be as a result of an application function or direct manipulation in a GUI",
        readOnly: false,
        readOnlyDescription: ''
    }, {
        displayName: 'Originator / Manual Entry',
        kind: "ORIGINATOR",
        nameDescription: "An originator is an application that captures data via a GUI or other input type (e.g. scanner, smartphone, card reader)",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows without any corresponding incoming flows'
    }
];


export default usageKinds;