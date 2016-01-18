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