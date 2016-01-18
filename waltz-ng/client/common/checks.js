import _ from 'lodash';


export function ensureIsArray(xs, message) {
    if (!_.isArray(xs)) {
        throw new Error(message ? message : 'not an array', xs);
    } else {
        return xs;
    }
}


export function ensureNotNull(x, message) {
    if (_.isNull(x) || _.isUndefined(x)) {
        throw new Error(message ? message : 'is null', x);
    } else {
        return x;
    }
}


export function ensureIsNumber(x, message) {
    const num = Number(x);
    if (_.isNaN(num)) {
        throw new Error(message ? message : `${x} is not a number`, x);
    } else {
        return num;
    }
}
