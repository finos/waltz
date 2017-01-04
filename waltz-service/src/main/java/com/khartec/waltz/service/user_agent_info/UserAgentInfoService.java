package com.khartec.waltz.service.user_agent_info;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.user_agent_info.UserAgentInfoDao;
import com.khartec.waltz.model.user_agent_info.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.*;

@Service
public class UserAgentInfoService {

    private final UserAgentInfoDao userAgentInfoDao;


    @Autowired
    public UserAgentInfoService(UserAgentInfoDao userAgentInfoDao) {
        checkNotNull(userAgentInfoDao, "userAgentInfoDao cannot be null");
        this.userAgentInfoDao = userAgentInfoDao;
    }


    public int save(UserAgentInfo userAgentInfo) {
        checkNotNull(userAgentInfo, "userLogin cannot be null");
        return userAgentInfoDao.save(userAgentInfo);
    }


    public List<UserAgentInfo> findLoginsForUser(String userName, int limit) {
        Checks.checkNotEmpty(userName, "username cannot be empty");
        checkTrue(limit > 0, "limit should be > 0");
        return userAgentInfoDao.findLoginsForUser(userName, limit);
    }

}
