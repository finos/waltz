package com.khartec.waltz.service.database;

import com.khartec.waltz.data.database_usage.DatabaseDao;
import com.khartec.waltz.model.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseService {

    private final DatabaseDao databaseDao;

    @Autowired
    public DatabaseService(DatabaseDao databaseDao) {
        this.databaseDao = databaseDao;
    }

    public List<Database> findByApplicationId(Long id) {
        checkNotNull(id, "id cannot be null");
        return databaseDao.findByApplicationId(id);
    }

    public Map<Long, List<Database>> findByApplicationIds(List<Long> ids) {
        checkNotNull(ids, "ids cannot be null");
        return databaseDao.findByApplicationIds(ids);
    }
}
