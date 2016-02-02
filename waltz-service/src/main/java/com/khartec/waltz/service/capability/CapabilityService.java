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

package com.khartec.waltz.service.capability;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class CapabilityService {

    private final CapabilityDao capabilityDao;

    @Autowired
    public CapabilityService(CapabilityDao capabilityDao) {
        checkNotNull(capabilityDao, "capabilityDao must not be null");
        this.capabilityDao = capabilityDao;
    }

    public List<Capability> findAll() {
        return capabilityDao.findAll();
    }


    public List<Capability> search(String query) {
        if (StringUtilities.isEmpty(query)) return Collections.emptyList();
        return capabilityDao.search(query);
    }


    public Forest<Capability, Long> buildHierarchy() {

        List<FlatNode<Capability, Long>> nodes = findAll()
                .stream()
                .map(c -> new FlatNode<>(c.id().get(), c.parentId(), c))
                .collect(Collectors.toList());

        return HierarchyUtilities.toForest(nodes);

    }


    public void assignLevels() {

        Forest<Capability, Long> forest = buildHierarchy();

        Map<Long, Integer> depths = HierarchyUtilities.assignDepths(forest);

        capabilityDao.assignLevels(depths);

    }

    public List<Capability> findByIds(Long[] capabilityIds) {
        return capabilityDao.findByIds(capabilityIds);
    }

    public List<Capability> findByAppIds(Long[] appIds) {
            return capabilityDao.findByAppIds(appIds);

    }
}



