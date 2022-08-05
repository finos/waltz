package org.finos.waltz.test_common_again.helpers;

import org.finos.waltz.model.actor.ImmutableActorCreateCommand;
import org.finos.waltz.service.actor.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActorHelper {

    @Autowired
    private ActorService actorService;

    public Long createActor(String nameStem) {
        return actorService.create(
                ImmutableActorCreateCommand
                        .builder()
                        .name(nameStem)
                        .description(nameStem + " Desc")
                        .isExternal(true)
                        .build(),
                "actorHelper");
    }
}
