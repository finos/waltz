package com.khartec.waltz.service.settings;

import com.khartec.waltz.data.settings.SettingsDao;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.model.settings.SettingKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class SettingsService {

    private final SettingsDao settingsDao;


    @Autowired
    public SettingsService(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }


    public Collection<Setting> findByKind(SettingKind kind) {
        return settingsDao.findByKind(kind);
    }


    public Setting getByName(String name) {
        return settingsDao.getByName(name);
    }

}
