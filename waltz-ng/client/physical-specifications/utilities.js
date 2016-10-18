import _ from 'lodash';


/**
 * Combines arrays of specifications, physicalFlows and logicalFlows into a single
 * array based around the physicalFlows and it's associated specification and logical
 * flow:
 *
 * ([specifications], [physicalFlows], [logicalFlows]) -> [ { physicalFlow, specification, logicalFlow } ... ]
 *
 * @param specifications
 * @param physicalFlows
 * @param logicalFlows
 * @returns {*}
 */
export function combineFlowData(specifications = [],
                                physicalFlows = [],
                                logicalFlows = [])
{
    return _.chain(specifications)
        .flatMap((a) => {
            const logicalById = _.keyBy(logicalFlows, "id");
            const relevantPhysicalFlows = _.filter(physicalFlows, { specificationId: a.id });
            if (relevantPhysicalFlows.length === 0) {
                return {
                    specification: a,
                    firstSpecification: true
                }
            } else {
                return _.flatMap(relevantPhysicalFlows, (pf, j) => {
                    return {
                        specification: a,
                        firstSpecification: j === 0,
                        physicalFlow: pf,
                        firstPhysicalFlow: j === 0,
                        logicalFlow: logicalById[pf.flowId]
                    };
                });
            }
        })
        .value();
}