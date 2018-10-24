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

package com.khartec.waltz.service.database_information;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.database_information.DatabaseInformationDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseInformationService {

    private final DatabaseInformationDao databaseInformationDao;
    private final ApplicationIdSelectorFactory factory;

    @Autowired
    public DatabaseInformationService(DatabaseInformationDao databaseInformationDao, ApplicationIdSelectorFactory factory) {
        Checks.checkNotNull(databaseInformationDao, "databaseInformationDao cannot be null");
        Checks.checkNotNull(factory, "factory cannot be null");

        this.databaseInformationDao = databaseInformationDao;
        this.factory = factory;
    }

    public List<DatabaseInformation> findByApplicationId(Long id) {
        checkNotNull(id, "id cannot be null");
        return databaseInformationDao.findByApplicationId(id);
    }

    public Map<Long, List<DatabaseInformation>> findByApplicationSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        return databaseInformationDao.findByAppSelector(factory.apply(options));
    }

    public DatabaseSummaryStatistics calculateStatsForAppIdSelector(IdSelectionOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        return databaseInformationDao.calculateStatsForAppSelector(factory.apply(options));
    }
        
}
