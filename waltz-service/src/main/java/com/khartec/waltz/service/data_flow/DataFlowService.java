/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.data_flow;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.data_flow.DataFlowStatsDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowStatistics;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceCalculator;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final ApplicationIdSelectorFactory idSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final AuthoritativeSourceCalculator authoritativeSourceCalculator;
    private ApplicationService applicationService;


    @Autowired
    public DataFlowService(ApplicationIdSelectorFactory idSelectorFactory,
                           ApplicationService applicationService,
                           AuthoritativeSourceCalculator authoritativeSourceCalculator,
                           DataFlowDao dataFlowDao,
                           DataFlowStatsDao dataFlowStatsDao,
                           DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(authoritativeSourceCalculator, "authoritativeSourceCalculator cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.idSelectorFactory = idSelectorFactory;
        this.applicationService = applicationService;
        this.authoritativeSourceCalculator = authoritativeSourceCalculator;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public List<DataFlow> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        return dataFlowDao.findByApplicationIdSelector(appIdSelector);
    }


    public int[] addFlows(List<DataFlow> flows) {
        List<DataFlow> flowsExceptSameSourceAndTarget = flows.stream()
                .filter(f -> !f.source().equals(f.target()))
                .collect(Collectors.toList());

        // need to retrieve the rating for each sourceApp, targetApp, datatype combination
        // first turn targetApps into owning orgUnits
        List<Long> targetApplicationIds = flowsExceptSameSourceAndTarget
                .stream()
                .map(df -> df.target().id())
                .collect(Collectors.toList());

        return dataFlowDao.addFlows(flowsExceptSameSourceAndTarget);
    }


    public int[] removeFlows(List<DataFlow> flows) {
        return dataFlowDao.removeFlows(flows);
    }


    public DataFlowStatistics calculateStats(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        List<StringTally> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes", () -> dataFlowStatsDao.tallyDataTypes(appIdSelector));
        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvement(appIdSelector));
        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvement(appIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }

    public DataFlowStatistics calculateStatsForDataType(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);

        List<StringTally> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes", () -> dataFlowStatsDao.tallyDataTypes(appIdSelector, dataTypeIdSelector));
        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvementByDataType(dataTypeIdSelector));
        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvementForDataTyoe(dataTypeIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }



    public List<Tally<String>> tallyByDataType() {
        return dataFlowDao.tallyByDataType();
    }


    public Collection<DataFlow> findByDataTypeIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);
        return dataFlowDao.findByDataTypeIdSelector(dataTypeIdSelector);
    }
}
