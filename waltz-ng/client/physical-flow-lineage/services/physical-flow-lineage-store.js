
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow-lineage`;

    const findByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}`)
        .then(r => r.data);

    const findContributionsByPhysicalFlowId = (id) => $http
        .get(`${base}/physical-flow/${id}/contributions`)
        .then(r => r.data);

    const findAllLineageReports = () => $http
        .get(`${base}/reports`)
        .then(r => r.data);


    const findLineageReportsBySelector = (options) => $http
        .post(`${base}/reports/selector`, options)
        .then(r => r.data);


    const removeContribution = (describedFlowId, contributorFlowId) => $http
        .delete(`${base}/physical-flow/${describedFlowId}/contributions/${contributorFlowId}`)
        .then(r => r.data);

    const addContribution = (describedFlowId, contributorFlowId) => $http
        .put(`${base}/physical-flow/${describedFlowId}/contributions`, contributorFlowId)
        .then(r => r.data);

    return {
        removeContribution,
        addContribution,
        findByPhysicalFlowId,
        findContributionsByPhysicalFlowId,
        findAllLineageReports,
        findLineageReportsBySelector
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
