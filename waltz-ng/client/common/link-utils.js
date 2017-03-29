
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
 * Given an entity kind, this will return the matching
 * ui-router state name if avaialble.  Otherwise it
 * will throw an error.
 * @param kind
 * @returns String state name
 */
export function kindToViewState(kind) {
    if (kind === 'APPLICATION') {
        return "main.app.view";
    }
    if (kind === 'ACTOR') {
        return "main.actor.view";
    }
    if (kind === 'APP_GROUP') {
        return "main.app-group.view";
    }
    if (kind === 'DATA_TYPE') {
        return "main.data-type.view";
    }
    if (kind === 'MEASURABLE') {
        return "main.measurable.view";
    }
    if (kind === 'ORG_UNIT') {
        return "main.org-unit.view";
    }
    if (kind === 'CHANGE_INITIATIVE') {
        return "main.change-initiative.view";
    }
    if (kind === 'ENTITY_STATISTIC') {
        return "main.entity-statistic.view";
    }
    if (kind === 'PERSON') {
        return "main.person.id";
    }
    if (kind === 'PROCESS') {
        return "main.process.view";
    }
    throw "Unable to convert kind: "+kind+ " to a ui-view state";
}


export function kindToBaseState(kind) {
    if (kind === 'CHANGE_INITIATIVE') {
        return "main.change-initiative";
    }
    throw "Unable to convert kind: "+kind+ " to a ui-base state";
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
        cellTemplate: `<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity['${entityRefField}']" icon-placement="'${iconPlacement}'"></waltz-entity-link></div>`
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

