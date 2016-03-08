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

package com.khartec.waltz.model.appview;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.trait.Trait;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutableAppView.class)
@JsonDeserialize(as = ImmutableAppView.class)
public abstract class AppView {

    public abstract Application app();
    public abstract List<String> tags();
    public abstract List<String> aliases();
    public abstract OrganisationalUnit organisationalUnit();
    public abstract Collection<Bookmark> bookmarks();
    public abstract Collection<ApplicationCapability> appCapabilities();
    public abstract Collection<Capability> capabilities();
    public abstract List<AssetCost> costs();
    public abstract List<Trait> explicitTraits();
}
