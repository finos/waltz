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

import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.data.capability.CapabilityIdSelectorFactory;
import com.khartec.waltz.data.capability.search.CapabilitySearchDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.capability.ImmutableCapability;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class CapabilityService {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityService.class);

    private final CapabilityDao capabilityDao;
    private final CapabilitySearchDao capabilitySearchDao;
    private final CapabilityIdSelectorFactory capabilityIdSelectorFactory;

    @Autowired
    public CapabilityService(CapabilityDao capabilityDao,
                             CapabilitySearchDao capabilitySearchDao,
                             CapabilityIdSelectorFactory capabilityIdSelectorFactory) {
        checkNotNull(capabilityDao, "capabilityDao must not be null");
        checkNotNull(capabilitySearchDao, "capabilitySearchDao must not be null");
        checkNotNull(capabilityIdSelectorFactory, "capabilityIdSelectorFactory cannot be null");

        this.capabilityDao = capabilityDao;
        this.capabilitySearchDao = capabilitySearchDao;
        this.capabilityIdSelectorFactory = capabilityIdSelectorFactory;
    }

    public List<Capability> findAll() {
        return capabilityDao.findAll();
    }


    public List<Capability> search(String query) {
        return capabilitySearchDao.search(query);
    }


    public Forest<Capability, Long> buildHierarchy() {

        List<FlatNode<Capability, Long>> nodes = findAll()
                .stream()
                .map(c -> new FlatNode<>(c.id().get(), c.parentId(), c))
                .collect(Collectors.toList());

        return HierarchyUtilities.toForest(nodes);

    }



    public List<Capability> findByIds(Long... capabilityIds) {
        return capabilityDao.findByIds(capabilityIds);
    }

    public List<Capability> findByAppIds(Long... appIds) {
            return capabilityDao.findByAppIds(appIds);

    }

    @Deprecated
    public boolean update(Capability capability) {
        return capabilityDao.update(capability);
    }


    @Deprecated
    public boolean rebuildHierarchy() {
        LOG.warn("Rebuilding capability hierarchy");

        assignLevels();
        Forest<Capability, Long> forest = buildHierarchy();

        for (Node<Capability, Long> node : forest.getAllNodes().values()) {
            Capability capability = node.getData();
            int currLevel = capability.level();

            Node ptr = node;
            while (ptr != null) {
                capability = setLevelField(capability, currLevel, ptr);
                ptr = ptr.getParent();
                currLevel--;
            }

            update(capability);
        }

        return true;
    }


    private void assignLevels() {
        Forest<Capability, Long> forest = buildHierarchy();
        Map<Long, Integer> depths = HierarchyUtilities.assignDepths(forest);
        capabilityDao.assignLevels(depths);
    }

    private static Capability setLevelField(Capability capability, int level, Node<Capability, Long> node) {
        if (level == 1) {
            return ImmutableCapability.copyOf(capability).withLevel1(node.getId());
        }
        if (level == 2) {
            return ImmutableCapability.copyOf(capability).withLevel2(node.getId());
        }
        if (level == 3) {
            return ImmutableCapability.copyOf(capability).withLevel3(node.getId());
        }
        if (level == 4) {
            return ImmutableCapability.copyOf(capability).withLevel4(node.getId());
        }
        if (level == 5) {
            return ImmutableCapability.copyOf(capability).withLevel5(node.getId());
        }
        return capability;
    }


    public Collection<Capability> findByIdSelector(IdSelectionOptions idSelectionOptions) {
        checkNotNull(idSelectionOptions, "idSelectionOptions cannot be null");

        Select<Record1<Long>> selector = capabilityIdSelectorFactory.apply(idSelectionOptions);
        return capabilityDao.findByIdSelector(selector);
    }
}



