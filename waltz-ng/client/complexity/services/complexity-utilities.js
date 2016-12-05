import _ from 'lodash';
import {notEmpty} from '../../common';


export function calcComplexitySummary(complexity = []) {
    if (!complexity) return;
    const cumulativeScore = _.sumBy(complexity, "overallScore");
    const averageScore = notEmpty(complexity) ? cumulativeScore / complexity.length : 0;

    return {
        cumulativeScore,
        averageScore
    };
}

