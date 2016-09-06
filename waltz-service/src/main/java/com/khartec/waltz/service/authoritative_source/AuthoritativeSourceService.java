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
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorRatingsService;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.service.data_type.DataTypeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AuthoritativeSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceService.class);

    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final DataFlowDecoratorRatingsService ratingService;
    private final DataFlowDecoratorService decoratorService;
    private final DataTypeService dataTypeService;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;


    @Autowired
    public AuthoritativeSourceService(AuthoritativeSourceDao authoritativeSourceDao,
                                      DataFlowDecoratorRatingsService ratingService,
                                      DataFlowDecoratorService dataFlowDecoratorService,
                                      DataTypeService dataTypeService,
                                      DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao must not be null");
        checkNotNull(ratingService, "ratingService cannot be null");
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.authoritativeSourceDao = authoritativeSourceDao;
        this.ratingService = ratingService;
        this.decoratorService = dataFlowDecoratorService;
        this.dataTypeService = dataTypeService;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        return authoritativeSourceDao.findByEntityKind(kind);
    }


    public AuthoritativeSource getById(long id) {
        return authoritativeSourceDao.getById(id);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        return authoritativeSourceDao.findByEntityReference(ref);
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        return authoritativeSourceDao.findByApplicationId(applicationId);
    }


    public int update(long id, Rating rating) {
        int updateCount = authoritativeSourceDao.update(id, rating);
        AuthoritativeSource updatedAuthSource = getById(id);
        ratingService.updateRatingsForAuthSource(updatedAuthSource.dataType(), updatedAuthSource.parentReference());
        return updateCount;
    }


    public int insert(EntityReference parentRef, String dataTypeCode, Long appId, Rating rating) {
        int insertedCount = authoritativeSourceDao.insert(parentRef, dataTypeCode, appId, rating);
        ratingService.updateRatingsForAuthSource(dataTypeCode, parentRef);
        return insertedCount;
    }


    public int remove(long id) {
        AuthoritativeSource authSourceToDelete = getById(id);
        int deletedCount = authoritativeSourceDao.remove(id);
        ratingService.updateRatingsForAuthSource(authSourceToDelete.dataType(), authSourceToDelete.parentReference());
        return deletedCount;
    }


    public List<AuthoritativeSource> findAll() {
        return authoritativeSourceDao.findAll();
    }


    public boolean recalculateAllFlowRatings() {
        findAll()
                .forEach(authSource -> ratingService.updateRatingsForAuthSource(
                        authSource.dataType(),
                        authSource.parentReference()));

        return true;
    }


    public List<AuthoritativeSource> findByDataTypeIdSelector(IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(idSelectionOptions);
        return authoritativeSourceDao.findByDataTypeIdSelector(selector);
    }
}
