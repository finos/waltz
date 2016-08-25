/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.service.data_flow.DataFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AuthoritativeSourceService {


    private final AuthoritativeSourceDao authoritativeSourceDao;
    private DataFlowService dataFlowService;


    @Autowired
    public AuthoritativeSourceService(AuthoritativeSourceDao authoritativeSourceDao,
                                      DataFlowService dataFlowService) {
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao must not be null");
        checkNotNull(dataFlowService, "dataFlowService cannot be null");

        this.authoritativeSourceDao = authoritativeSourceDao;
        this.dataFlowService = dataFlowService;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        return authoritativeSourceDao.findByEntityKind(kind);
    }


    public AuthoritativeSource getById(long id) {
        return authoritativeSourceDao.getById(id);
    }


    public List<AuthoritativeSource> findByEntityReferences(EntityKind kind, List<Long> ids) {
        return authoritativeSourceDao.findByEntityReferences(kind, ids);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        return authoritativeSourceDao.findByEntityReference(ref);
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        return authoritativeSourceDao.findByApplicationId(applicationId);
    }


    public int update(long id, Rating rating) throws SQLException {
        int updateCount = authoritativeSourceDao.update(id, rating);
        AuthoritativeSource updatedAuthSource = getById(id);
        updateRatingsForAuthSource(updatedAuthSource);
        return updateCount;
    }


    public int insert(EntityReference parentRef, String dataType, Long appId, Rating rating) throws SQLException {
        int insertedCount = authoritativeSourceDao.insert(parentRef, dataType, appId, rating);
        dataFlowService.updateRatingsForConsumers(parentRef, dataType, appId);
        return insertedCount;
    }


    public int remove(long id) throws SQLException {

        AuthoritativeSource authSourceToDelete = getById(id);
        int deletedCount = authoritativeSourceDao.remove(id);
        updateRatingsForAuthSource(authSourceToDelete);
        return deletedCount;
    }


    private void updateRatingsForAuthSource(AuthoritativeSource authSource) throws SQLException {
        dataFlowService.updateRatingsForConsumers(
                authSource.parentReference(),
                authSource.dataType(),
                authSource.applicationReference().id());
    }

}
