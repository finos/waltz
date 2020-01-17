import _ from "lodash";

export function countByVersionId(usages = []) {
    // console.time('counting usages')
    const countsByVersionId =  _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwareVersionId", "applicationId"])))
        .uniqBy(u => "v:" + u.softwareVersionId + "_a:" + u.applicationId)
        .countBy(u => u.softwareVersionId)
        .value();

    // console.timeEnd('counting usages')
    return countsByVersionId;
}