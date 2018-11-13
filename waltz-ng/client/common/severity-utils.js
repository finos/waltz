const severityMap = {
    "INFORMATION": {
        bootstrapClass: "info",
        score: 0
    },
    "WARNING": {
        bootstrapClass: "warning",
        score: 1
    },
    "ERROR": {
        bootstrapClass: "danger",
        score: 2
    }
};


const defaultSeverity = severityMap.INFORMATION;


function lookupBySeverity(severity) {
    return _.get(severityMap, [ severity ], defaultSeverity);
}

/**
 * Given a severity from the `com.khartec.waltz.model.Severity`
 * enum will return it's corresponding bootstrap classname
 * @param severity
 * @returns {string}
 */
export function severityToBootstrapAlertClass(severity) {
    return lookupBySeverity(severity).bootstrapClass;
}


export function severityToBootstrapBtnClass(severity) {
    return `btn-${lookupBySeverity(severity).bootstrapClass}`;
}


export function findHighestSeverity(severities = []) {
    return _
        .maxBy(
            severities,
            s => _.get(severityMap, [s], defaultSeverity).score);
}
