package com.khartec.waltz.service.server_usage;

import com.khartec.waltz.data.server_usage.ServerUsageDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.server_usage.ServerUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ServerUsageService {

    private final ServerUsageDao serverUsageDao;


    @Autowired
    public ServerUsageService(ServerUsageDao serverUsageDao) {
        checkNotNull(serverUsageDao, "serverUsageDao cannot be null");
        this.serverUsageDao = serverUsageDao;
    }


    public Collection<ServerUsage> findByServerId(long serverId) {
        return serverUsageDao.findByServerId(serverId);
    }


    public Collection<ServerUsage> findByReferencedEntity(EntityReference ref) {
        return serverUsageDao.findByReferencedEntity(ref);
    }
}
