package com.khartec.waltz.service.user;

import com.khartec.waltz.data.user.UserRoleDao;
import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromArray;

/**
 * Created by dwatkins on 30/03/2016.
 */
@Service
public class UserRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleService.class);

    private final UserRoleDao userRoleDao;


    @Autowired
    public UserRoleService(UserRoleDao userRoleDao) {
        checkNotNull(userRoleDao, "userRoleDao must not be null");

        this.userRoleDao = userRoleDao;
    }



    public boolean hasRole(String userName, Role... requiredRoles) {
        List<Role> userRoles = userRoleDao.getUserRoles(userName);
        return userRoles.containsAll(fromArray(requiredRoles));
    }


    public List<User> findAllUsers() {
        return userRoleDao.findAllUsers();
    }


    public User findByUserName(String userName) {
        return ImmutableUser.builder()
                .userName(userName)
                .addAllRoles(userRoleDao.getUserRoles(userName))
                .build();
    }


    public boolean updateRoles(String userName, List<Role> newRoles) {
        LOG.info("Updating roles for userName: "+userName + ", new roles: " + newRoles);
        return userRoleDao.updateRoles(userName, newRoles);
    }


    public List<Role> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }

}
