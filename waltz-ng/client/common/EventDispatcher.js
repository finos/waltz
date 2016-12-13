/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';

export default class EventDispatcher {
    subscribers = [];

    /**
     * Sends the given event to all matching subscriber.
     * A matching subscriber either matches the events
     * type field or has subscribed to all event types ('*')
     * @param e
     */
    dispatch(e) {
        _.each(this.subscribers, ({callback, type}) => {
            if (type === '*' || e.type === type ) {
                callback(e);
            }
        });
    }

    /**
     * Subscribe to the event dispatcher with
     * an optional type filter.  This function
     * returns an unsubscribe function.
     * @param callback
     * @param type, defaults to all ('*')
     * @returns {Function}
     */
    subscribe(callback, type = '*') {
        const item = {callback, type};
        this.subscribers.push(item);
        return () => _.reject(this.subscribers, s => s === item);
    }
}