import _ from "lodash";
import apiCheck from "api-check";


const entityRefShape = {
    id: apiCheck.number,
    kind: apiCheck.string
};


export const checkIsEntityRef = apiCheck
    .shape(entityRefShape);


export const checkIsStringList = apiCheck
    .arrayOf(apiCheck.string);


export const checkIsApplicationIdSelector = apiCheck
    .shape({
        entityReference: apiCheck.shape(entityRefShape),
        scope: apiCheck.oneOf(['EXACT', 'PARENTS', 'CHILDREN'])
    });


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
