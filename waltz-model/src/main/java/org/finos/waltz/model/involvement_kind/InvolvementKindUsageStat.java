package org.finos.waltz.model.involvement_kind;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

import javax.swing.text.html.parser.Entity;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementKindUsageStat.class)
public abstract class InvolvementKindUsageStat {

    @Value.Immutable
    public interface Stat {
        EntityKind entityKind();

        boolean isCountOfRemovedPeople();

        int personCount();
    }

    public abstract Set<Stat> breakdown();

    public abstract InvolvementKind involvementKind();

}
