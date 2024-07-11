package org.finos.waltz.service.permission;

import org.finos.waltz.data.permission.PermissionViewDao;
import org.finos.waltz.model.permission.PermissionViewItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionViewService {
    private final PermissionViewDao permissionViewDao;


    @Autowired
    public PermissionViewService(PermissionViewDao permissionViewDao) {
        this.permissionViewDao = permissionViewDao;
    }


    public Set<PermissionViewItem> findAll() {
        return permissionViewDao.findAll();
    }

}
