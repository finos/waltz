package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.actor.ImmutableActorCreateCommand;
import org.finos.waltz.service.actor.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class ActorHelper {

    @Autowired
    private ActorService actorService;

    public Long createActor(String nameStem) {
        return actorService.create(
                ImmutableActorCreateCommand
                        .builder()
                        .name(nameStem)
                        .externalId(mkName(nameStem))
                        .description(nameStem + " Desc")
                        .isExternal(true)
                        .build(),
                "actorHelper");
    }
}
