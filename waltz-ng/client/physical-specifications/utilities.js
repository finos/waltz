import _ from 'lodash';


/**
 * Combines arrays of specifications, physicalFlows and logicalFlows into a single
 * array based around the physicalFlows and it's associated specification and logical
 * flow:
 *
 * ([specifications], [physicalFlows], [entityRefs]) -> [ { physicalFlow, specification, sourceRef, targetRef } ... ]
 *
 * @param specifications
 * @param physicalFlows
 * @param logicalFlows
 * @returns {*}
 */
export function combineFlowData(specifications = [],
                                physicalFlows = [])
{
    if (specifications.length === 0 || physicalFlows.length === 0) {
        return [];
    } else {
        return _.chain(specifications)
            .flatMap((s) => {
                const relevantPhysicalFlows = _.filter(physicalFlows, { specificationId: s.id });
                const sourceRef = s.owningEntity;

                if (sourceRef == null) {
                    return null;
                }
                if (relevantPhysicalFlows.length === 0) {
                    return {
                        sourceRef,
                        specification: s,
                        firstSpecification: true
                    }
                } else {
                    return _.flatMap(relevantPhysicalFlows, (pf, j) => {
                        const targetRef = pf.target;

                        if (targetRef == null) { return null; }
                        return {
                            specification: s,
                            physicalFlow: pf,
                            firstPhysicalFlow: j === 0,
                            firstSpecification: j === 0,
                            sourceRef,
                            targetRef
                        };
                    });
                }
            })
            .filter(r => r !== null)
            .value();
    }
}


export function enrichConsumes(specifications = [],
                        physicalFlows = [])
{
    const visitedRefs = [];

    if (specifications.length === 0 || physicalFlows.length === 0) {
        return [];
    } else {
        return _.chain(specifications)
            .map(specification => {
                const physicalFlow = _.find(physicalFlows, {specificationId: specification.id});
                const sourceRef = specification.owningEntity;
                const targetRef = physicalFlow.target.id;

                if (!physicalFlow || !sourceRef || !targetRef) {
                    return null;
                } else {
                    const firstSource = !_.includes(
                        visitedRefs,
                        sourceRef);

                    if (firstSource === true) {
                        visitedRefs.push(sourceRef);
                    }

                    return {
                        specification,
                        physicalFlow,
                        firstSource,
                        sourceRef,
                        targetRef
                    };
                }
            })
            .filter(r => r != null)
            .value();
    }
}
