package com.khartec.waltz.service.user;

import com.khartec.waltz.data.user.UserPreferenceDao;
import com.khartec.waltz.model.user.UserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class UserPreferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceService.class);

    private final UserPreferenceDao userPreferenceDao;


    @Autowired
    public UserPreferenceService(UserPreferenceDao userPreferenceDao) {
        checkNotNull(userPreferenceDao, "userPreferenceDao cannot be null");

        this.userPreferenceDao = userPreferenceDao;
    }


    public List<UserPreference> getPreferences(String userId) {
        return userPreferenceDao.getPreferencesForUser(userId);
    }


    public boolean savePreferences(String userId, List<UserPreference> preferences) {
        userPreferenceDao.savePreferencesForUser(userId, preferences);
        return true;
    }


    public boolean clearPreferences(String userId) {
        userPreferenceDao.clearPreferencesForUser(userId);
        return true;
    }

}
