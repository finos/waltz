package com.khartec.waltz.service.database;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.database_usage.DatabaseDao;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.database.Database;
import com.khartec.waltz.model.database.DatabaseSummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseService {

    private final DatabaseDao databaseDao;
    private final ApplicationIdSelectorFactory factory;

    @Autowired
    public DatabaseService(DatabaseDao databaseDao, ApplicationIdSelectorFactory factory) {
        Checks.checkNotNull(databaseDao, "databaseDao cannot be null");
        Checks.checkNotNull(factory, "factory cannot be null");

        this.databaseDao = databaseDao;
        this.factory = factory;
    }

    public List<Database> findByApplicationId(Long id) {
        checkNotNull(id, "id cannot be null");
        return databaseDao.findByApplicationId(id);
    }

    public Map<Long, List<Database>> findByApplicationSelector(ApplicationIdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        return databaseDao.findByAppSelector(factory.apply(options));
    }

    public DatabaseSummaryStatistics findStatsForAppIdSelector(ApplicationIdSelectionOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        return databaseDao.findStatsForAppSelector(factory.apply(options));
    }
        
}
