package com.khartec.waltz.service.settings;

import com.khartec.waltz.data.settings.SettingsDao;
import com.khartec.waltz.model.settings.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;


@Service
public class SettingsService {

    private final SettingsDao settingsDao;

    public static final String DEFAULT_ROLES_KEY = "server.authentication.roles.default";


    @Autowired
    public SettingsService(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }


    public Collection<Setting> findAll() {
        return settingsDao.findAll();
    }


    public Setting getByName(String name) {
        return settingsDao.getByName(name);
    }

    /**
     * Helper method which looks up the setting and returns
     * it's value (or empty if either not found or the value is null)
     * @param name
     * @return
     */
    public Optional<String> getValue(String name) {
        Setting setting = settingsDao.getByName(name);
        return setting == null
            ? Optional.empty()
            : setting.value();
    }

}
