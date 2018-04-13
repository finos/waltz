package com.khartec.waltz.service.shared_preference;


import com.khartec.waltz.data.shared_preference.SharedPreferenceDao;
import com.khartec.waltz.model.shared_preference.ImmutableSharedPreference;
import com.khartec.waltz.model.shared_preference.SharedPreference;
import com.khartec.waltz.model.shared_preference.SharedPreferenceSaveCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;

@Service
public class SharedPreferenceService {
    
    private final SharedPreferenceDao sharedPreferenceDao;
    

    @Autowired
    public SharedPreferenceService(SharedPreferenceDao sharedPreferenceDao) {
        checkNotNull(sharedPreferenceDao, "sharedPreferenceDao cannot be null");
        this.sharedPreferenceDao = sharedPreferenceDao;
    }


    public SharedPreference getPreference(String key, String category) {
        checkNotEmpty(key, "key cannot be empty");
        checkNotEmpty(category, "category cannot be empty");
        return sharedPreferenceDao.getPreference(key, category);
    }


    public List<SharedPreference> findPreferencesByCategory(String category) {
        checkNotEmpty(category, "category cannot be empty");
        return sharedPreferenceDao.findPreferencesByCategory(category);
    }


    public boolean savePreference(String username, SharedPreferenceSaveCommand command) {
        checkNotEmpty(username, "username cannot be empty");
        checkNotNull(command, "sharedPreference cannot be null");

        SharedPreference sharedPreference = ImmutableSharedPreference.builder()
                .key(command.key())
                .category(command.category())
                .value(command.value())
                .lastUpdatedBy(username)
                .lastUpdatedAt(nowUtc())
                .build();

        return sharedPreferenceDao.savePreference(sharedPreference);
    }
}
