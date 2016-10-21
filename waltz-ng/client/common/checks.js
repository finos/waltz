import _ from "lodash";
import apiCheck from "api-check";


const myApiCheck = apiCheck({ verbose: false });


const entityRefShape = {
    id: apiCheck.number,
    kind: apiCheck.string
};

const createLineageReportCommandShape = {
    specificationId: apiCheck.number,
    name: apiCheck.string
};

const createActorCommandShape = {
    name: apiCheck.string,
    description: apiCheck.string
}


const check = (test, x) => myApiCheck.throw(test, x);


export const checkIsEntityRef = ref =>
    check(myApiCheck.shape(entityRefShape), ref);


export const checkIsCreateLineageReportCommand = ref =>
    check(myApiCheck.shape(createLineageReportCommandShape), ref);


export const checkIsCreateActorCommand = ref =>
    check(myApiCheck.shape(createActorCommandShape), ref);


export const checkIsStringList = xs =>
    check(myApiCheck.arrayOf(apiCheck.string), xs);


/* @Deprecated - use checkIsIdSelector instead */
export const checkIsApplicationIdSelector = opt =>
    check(
        myApiCheck.shape({
            entityReference: myApiCheck.shape(entityRefShape),
            scope: myApiCheck.oneOf(['EXACT', 'PARENTS', 'CHILDREN'])}),
        opt);


export const checkIsIdSelector = checkIsApplicationIdSelector;


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
