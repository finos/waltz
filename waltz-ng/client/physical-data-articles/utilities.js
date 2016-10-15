/**
 * Combines arrays of articles, physicalFlows and logicalFlows into a single
 * array based around the physicalFlows and it's associated article and logical
 * flow:
 *
 * ([articles], [physicalFlows], [logicalFlows]) -> [ { physicalFlow, article, logicalFlow } ... ]
 *
 * @param articles
 * @param physicalFlows
 * @param logicalFlows
 * @returns {*}
 */
export function combineFlowData(articles = [],
                                physicalFlows = [],
                                logicalFlows = [])
{
    return _.chain(articles)
        .flatMap((a) => {
            const logicalById = _.keyBy(logicalFlows, "id");
            const relevantPhysicalFlows = _.filter(physicalFlows, { articleId: a.id });
            if (relevantPhysicalFlows.length === 0) {
                return {
                    article: a,
                    firstArticle: true
                }
            } else {
                return _.flatMap(relevantPhysicalFlows, (pf, j) => {
                    return {
                        article: a,
                        firstArticle: j === 0,
                        physicalFlow: pf,
                        firstPhysical: j === 0,
                        logicalFlow: logicalById[pf.flowId]
                    };
                });
            }
        })
        .value();
}