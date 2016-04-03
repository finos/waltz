package com.khartec.waltz.web.endpoints.auth;

import com.khartec.waltz.service.settings.SettingsService;
import spark.Filter;

import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;


public abstract class WaltzFilter implements Filter {

    private final SettingsService settingsService;


    public WaltzFilter(SettingsService settingsService) {
        checkNotNull(settingsService, "Settings Service cannot be null");
        this.settingsService = settingsService;
    }


    public Optional<String> getSettingValue(String name) {
        return settingsService.getValue(name);
    }


}
