package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.model.settings.SettingKind;
import com.khartec.waltz.service.settings.SettingsService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readEnum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

/**
 * Created by dwatkins on 30/03/2016.
 */
@Service
public class SettingsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "settings");

    private final SettingsService settingsService;


    @Autowired
    public SettingsEndpoint(SettingsService settingsService) {
        this.settingsService = settingsService;
    }


    @Override
    public void register() {
        String findByKindPath = mkPath(BASE_URL, "kind", ":kind");
        String getByNamePath = mkPath(BASE_URL, "name", ":name");

        ListRoute<Setting> findByKindRoute = (request, response) ->
                sanitize(settingsService.findByKind(
                        readEnum(request, "kind", SettingKind.class, SettingKind.WEB)));

        DatumRoute<Setting> getByNameRoute = (request, response) ->
                sanitize(settingsService.getByName(
                        request.params("name")));

        getForList(findByKindPath, findByKindRoute);
        getForDatum(getByNamePath, getByNameRoute);
    }


    private Collection<Setting> sanitize(Collection<Setting> settings) {
        return settings
            .stream()
            .map(s -> sanitize(s))
            .collect(Collectors.toList());
    }


    private Setting sanitize(Setting s) {
        return s.kind() == SettingKind.HIDDEN
                ? ImmutableSetting.copyOf(s).withValue("****")
                : s;
    }

}
