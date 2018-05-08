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
 * Creates a column def to render an entity link
 *
 * eg: usage: mkEntityLinkGridCell('Source', 'source', 'none')
 *
 * @param columnHeading column display name
 * @param entityRefField field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param iconPlacement icon position, allowed values: left, right, none
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEnumGridCell(columnHeading, entityRefField, showIcon = false) {
    return {
        field: entityRefField,
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-enum-value type="'AuthoritativenessRating'"
                                  show-icon="${showIcon}"
                                  key="COL_FIELD">
                </waltz-enum-value>
            </div>`
    };
}




/**
 * Creates a column def to render an entity link
 *
 * eg: usage: mkEntityLinkGridCell('Source', 'source', 'none')
 *
 * @param columnHeading column display name
 * @param entityRefField field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param iconPlacement icon position, allowed values: left, right, none
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEntityLinkGridCell(columnHeading, entityRefField, iconPlacement = 'left') {
    return {
        field: entityRefField + '.name',
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-entity-link entity-ref="row.entity.${entityRefField}" 
                                   icon-placement="'${iconPlacement}'">
                </waltz-entity-link>
            </div>`
    };
}


/**
 * Creates a column def to render a link with an id parameter
 *
 * @param columnHeading column display name
 * @param displayField field name that stores the value to be displayed on the grid
 * @param linkIdField field name that stores the link id field
 * @param linkNavViewName navigation view name
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkLinkGridCell(columnHeading, displayField, linkIdField, linkNavViewName) {
    return {
        field: displayField,
        displayName: columnHeading,
        cellTemplate: `<div class="ui-grid-cell-contents">\n<a ui-sref="${linkNavViewName} ({ id: row.entity.${linkIdField} })" ng-bind="COL_FIELD">\n</a>\n</div>`
    };
}

