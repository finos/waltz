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


    public List<UserPreference> getPreferences(String userName) {
        return userPreferenceDao.getPreferencesForUser(userName);
    }


    public List<UserPreference> savePreferences(String userName, List<UserPreference> preferences) {
        userPreferenceDao.savePreferencesForUser(userName, preferences);
        return getPreferences(userName);
    }


    public List<UserPreference> savePreference(UserPreference preference) {
        userPreferenceDao.savePreference(preference);
        return getPreferences(preference.userName());
    }


    public boolean clearPreferences(String userName) {
        userPreferenceDao.clearPreferencesForUser(userName);
        return true;
    }

}
