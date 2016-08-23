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

    public DatabaseSummaryStatistics findStatsForAppIdSelector(IdSelectionOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        return databaseInformationDao.findStatsForAppSelector(factory.apply(options));
    }
        
}
