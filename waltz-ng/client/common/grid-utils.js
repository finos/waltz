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
 * Creates a column def to render an entity link
 *
 * eg: usage: mkEntityLinkGridCell('Source', 'source', 'none')
 *
 * @param columnHeading  column display name
 * @param entityRefField  field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param showIcon  whether to display the icon or not
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEnumGridCell(columnHeading,
                               entityRefField,
                               enumType,
                               showIcon = false,
                               showPopover = false) {
    return {
        field: entityRefField,
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-enum-value type="'${enumType}'"
                                  show-icon="${showIcon}"
                                  show-popover="${showPopover}"
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
 * @param columnHeading  column display name
 * @param entityRefField  field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param iconPlacement  icon position, allowed values: left, right, none
 * @param tooltipPlacement  position of tooltip, allowed values are: left, right, bottom, top
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEntityLinkGridCell(columnHeading,
                                     entityRefField,
                                     iconPlacement = "left",
                                     tooltipPlacement = "top",
                                     additionalProps = {},
                                     isSecondaryLink = false) {
    return Object.assign(
        {},
        additionalProps,
        {
            field: entityRefField + ".name",
            displayName: columnHeading,
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-entity-link entity-ref="row.entity.${entityRefField}"
                                       tooltip-placement="${tooltipPlacement}"
                                       icon-placement="${iconPlacement}"
                                       is-secondary-link="${isSecondaryLink}">
                    </waltz-entity-link>
                </div>`
        });
}


/**
 * Creates a column def to render an entity icon and label
 *
 * eg: usage: mkEntityLabelGridCell('Source', 'source', 'none')
 *
 * @param columnHeading  column display name
 * @param entityRefField  field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param iconPlacement  icon position, allowed values: left, right, none
 * @param tooltipPlacement  position of tooltip, allowed values are: left, right, bottom, top
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEntityLabelGridCell(columnHeading,
                                      entityRefField,
                                      iconPlacement = "left",
                                      tooltipPlacement = "top") {
    return {
        field: entityRefField + ".name",
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-entity-icon-label entity-ref="row.entity.${entityRefField}"
                                         tooltip-placement="${tooltipPlacement}"
                                         icon-placement="${iconPlacement}">
                </waltz-entity-icon-label>
            </div>`
    };
}


/**
 * Creates a column def to render a link with an id parameter
 *
 * @param columnHeading  column display name
 * @param displayField  field name that stores the value to be displayed on the grid
 * @param linkIdField  field name that stores the link id field
 * @param linkNavViewName  navigation view name
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkLinkGridCell(columnHeading,
                               displayField,
                               linkIdField,
                               linkNavViewName,
                               additionalProps = {}) {
    return Object.assign({}, additionalProps, {
        field: displayField,
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <a ui-sref="${linkNavViewName} ({ id: row.entity.${linkIdField} })"
                    ng-if="COL_FIELD"
                   ng-bind="COL_FIELD">
                </a>
                <a ui-sref="${linkNavViewName} ({ id: row.entity.${linkIdField} })"
                    ng-if="!COL_FIELD">
                    -
                </a>
            </div>`
    });
}

/**
 * Creates a column def to render date
 *
 *
 * @param columnHeading  column display name
 * @param entityRefField  field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param showIcon  whether to display the icon or not
 * @param dateOnly  whether to display the only date or time duration from now
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkDateGridCell(columnHeading,
                               dateField,
                               showIcon = false,
                               dateOnly = false) {
    return {
        field: dateField,
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-from-now ng-if="!${dateOnly}" timestamp="COL_FIELD"></waltz-from-now>
                <waltz-from-now ng-if="${dateOnly}" timestamp="COL_FIELD" date-only="${dateOnly}"></waltz-from-now>
            </div>`
    };
}


