package org.finos.waltz.service.involvement_group;

import org.finos.waltz.data.involvement_group.InvolvementGroupDao;
import org.finos.waltz.model.involvement_group.InvolvementGroupCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class InvolvementGroupService {

    public final InvolvementGroupDao involvementGroupDao;

    @Autowired
    public InvolvementGroupService(InvolvementGroupDao involvementGroupDao) {
        this.involvementGroupDao = involvementGroupDao;
    }

    public long createGroup(InvolvementGroupCreateCommand groupCreateCommand, String username) {
        return involvementGroupDao.createInvolvementGroup(groupCreateCommand);
    }

    public void updateInvolvements(Long groupId, Set<Long> involvementKindIds, String userName) {
        involvementGroupDao.updateInvolvements(groupId, involvementKindIds);
    }
}
