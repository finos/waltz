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

package com.khartec.waltz.service.capability_rating;

import com.khartec.waltz.data.capability_rating.CapabilityRatingDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.capabilityrating.RatingChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class CapabilityRatingService {

    private final CapabilityRatingDao dao;


    @Autowired
    public CapabilityRatingService(CapabilityRatingDao dao) {
        checkNotNull(dao, "dao must not be null");
        this.dao = dao;
    }


    public List<CapabilityRating> findByParent(EntityReference parentRef) {
        return dao.findByParent(parentRef);
    }


    public List<CapabilityRating> findByParentAndPerspective(EntityReference parentRef, String perspectiveCode) {
        return dao.findByParentAndPerspective(parentRef, perspectiveCode);
    }


    public int update(AppRatingChangesAction changesAction) {
        List<RatingChange> changes = changesAction.changes();

        List<RatingChange> updates = new ArrayList<>();
        List<RatingChange> creates = new ArrayList<>();
        List<RatingChange> deletes = new ArrayList<>();

        for (RatingChange change : changes) {
            if (change.original() == RagRating.Z) {
                creates.add(change);
            } else if (change.current() == RagRating.Z) {
                deletes.add(change);
            } else {
                updates.add(change);
            }
        }

        int[] updated = dao.update(changesAction.application(), changesAction.perspectiveCode(), updates);
        int[] created = dao.create(changesAction.application(), changesAction.perspectiveCode(), creates);
        int[] deleted = dao.delete(changesAction.application(), changesAction.perspectiveCode(), deletes);

        return updated.length + created.length + deleted.length;
    }

    public List<CapabilityRating> findByCapabilityIds(List<Long> capIds) {
        return dao.findByCapabilityIds(capIds);

    }

    public List<CapabilityRating> findByAppIds(Long[] appIds) {
        return dao.findByAppIds(appIds);

    }
}
