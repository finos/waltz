/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import {
    criticalityDisplayNames,
    investmentRatingNames,
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames
} from "../common/services/display-names";


export function mapToDisplayNames(app) {
    return {
        kindDisplay: applicationKindDisplayNames[app.applicationKind] || app.applicationKind,
        overallRatingDisplay: investmentRatingNames[app.overallRating] || app.overallRating,
        businessCriticalityDisplay: criticalityDisplayNames[app.businessCriticality] || app.businessCriticality,
        riskRatingDisplay: criticalityDisplayNames[app.riskRating] || app.riskRating,
        lifecyclePhaseDisplay: lifecyclePhaseDisplayNames[app.lifecyclePhase] || app.lifecyclePhase
    }
}