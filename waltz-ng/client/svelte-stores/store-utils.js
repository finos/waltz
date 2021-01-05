import _ from "lodash";

export function anyErrors(xs) {
    return {
        hasErrors: _.some(xs, v => v.status === "error"),
        errors: _.chain(xs).map(x => x.error).reject(_.isNil).value()
    };
}

export function anyLoading(xs) {
    return _.some(xs, v => v.status === "loading");
}


export function mkOverallStatus(xs) {
    return {
        ...anyErrors(xs),
        isLoading: anyLoading(xs)
    };
}