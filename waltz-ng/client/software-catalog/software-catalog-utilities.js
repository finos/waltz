import _ from "lodash";

export function groupByVersionId(usages = []) {
    const usagesByVersionId = _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwarePackageId", "softwareVersionId", "applicationId"])))
        .uniqWith(_.isEqual)
        .groupBy(u => u.softwareVersionId)
        .value();

    return usagesByVersionId;
}