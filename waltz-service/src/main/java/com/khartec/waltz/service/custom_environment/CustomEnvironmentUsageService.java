package com.khartec.waltz.service.custom_environment;

import com.khartec.waltz.data.custom_environment.CustomEnvironmentUsageDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsage;
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CustomEnvironmentUsageService {

    private final CustomEnvironmentUsageDao customEnvironmentUsageDao;

    @Autowired
    public CustomEnvironmentUsageService(CustomEnvironmentUsageDao customEnvironmentUsageDao){
        this.customEnvironmentUsageDao = customEnvironmentUsageDao;
    }


    public Set<CustomEnvironmentUsage> findUsagesByOwningEntityRef(EntityReference ref) {
        return customEnvironmentUsageDao.findByOwningEntityRef(ref);
    }


    public Long addAsset(CustomEnvironmentUsage usage, String username){
        return customEnvironmentUsageDao.addAsset(usage, username);
    }


    public Boolean remove(Long usageId, String username){
        return customEnvironmentUsageDao.remove(usageId);
    }


    public Set<CustomEnvironmentUsageInfo> findUsageInfoByOwningEntity(EntityReference ref){
        return customEnvironmentUsageDao.findUsageInfoByOwningRef(ref);
    }


}

