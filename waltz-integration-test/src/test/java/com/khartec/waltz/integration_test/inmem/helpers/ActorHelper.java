package com.khartec.waltz.integration_test.inmem.helpers;

import org.finos.waltz.model.actor.ImmutableActorCreateCommand;
import org.finos.waltz.service.actor.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;

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
