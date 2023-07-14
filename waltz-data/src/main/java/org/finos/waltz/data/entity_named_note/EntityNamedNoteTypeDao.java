/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.entity_named_note;

import org.finos.waltz.common.Checks;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.EntityWithOperations;
import org.finos.waltz.model.ImmutableEntityWithOperations;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.app_group.AppGroupMemberRole;
import org.finos.waltz.model.entity_named_note.EntityNamedNodeType;
import org.finos.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import org.finos.waltz.model.entity_named_note.ImmutableEntityNamedNodeType;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.tables.ApplicationGroupMember;
import org.finos.waltz.schema.tables.EntityNamedNote;
import org.finos.waltz.schema.tables.EntityNamedNoteType;
import org.finos.waltz.schema.tables.UserRole;
import org.finos.waltz.schema.tables.records.EntityNamedNoteTypeRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.minus;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.common.StringUtilities.splitThenMap;
import static org.finos.waltz.model.RoleUtilities.getRequiredRoleForEntityKind;
import static org.finos.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;
import static org.finos.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;
import static org.finos.waltz.schema.tables.EntityNamedNoteType.ENTITY_NAMED_NOTE_TYPE;
import static org.finos.waltz.schema.tables.UserRole.USER_ROLE;

@Repository
public class EntityNamedNoteTypeDao {

    private static final String SEPARATOR = ";";


    private static final EntityNamedNoteType ennt = ENTITY_NAMED_NOTE_TYPE;
    private static final RecordMapper<Record, EntityNamedNodeType> TO_DOMAIN_MAPPER = record -> {

        EntityNamedNoteTypeRecord r = record.into(ennt);

        List<EntityKind> applicableEntityKinds = splitThenMap(
                r.getApplicableEntityKinds(),
                SEPARATOR,
                EntityKind::valueOf);

        return ImmutableEntityNamedNodeType
                .builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .addAllApplicableEntityKinds(applicableEntityKinds)
                .isReadOnly(r.getIsReadonly())
                .position(r.getPosition())
                .externalId(Optional.ofNullable(r.getExternalId()))
                .build();
    };
    private static final EntityNamedNote enn = ENTITY_NAMED_NOTE;
    private static final UserRole ur = USER_ROLE;
    private static final ApplicationGroupMember agm = APPLICATION_GROUP_MEMBER;


    private final DSLContext dsl;


    @Autowired
    public EntityNamedNoteTypeDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityNamedNodeType> findAll() {
        return dsl
                .select(ennt.fields())
                .from(ennt)
                .fetch(TO_DOMAIN_MAPPER);
    }


    /**
     * Attempts to remove the named note type specified by the
     * given id.  The removal will only take place if no
     * entity named notes refer to this id.
     *
     * @param id - identifier of the named note type to remove
     * @return boolean - whether the note was removed.
     */
    public boolean removeById(Long id) {

        SelectConditionStep<Record1<Long>> anyUsageOfType = DSL
                .select(enn.ENTITY_ID)
                .from(enn)
                .where(enn.NAMED_NOTE_TYPE_ID.eq(id));

        return dsl
                .deleteFrom(ennt)
                .where(ennt.ID.eq(id))
                .andNotExists(anyUsageOfType)
                .execute() == 1;
    }


    /**
     * Creates a new record and returns the generated id.  All fields in the command must be
     * provided.
     * @param command an EntityNameNoteTypeChangeCommand object
     * @return the id of the note type created.
     */
    public long create(EntityNamedNoteTypeChangeCommand command) {
        String name = Checks.checkOptionalIsPresent(command.name(), "Name must be provided");
        Set<EntityKind> applicableEntityKinds = Checks.checkOptionalIsPresent(command.applicableEntityKinds(), "Applicable Entity Kinds must be provided");
        String kinds = join(applicableEntityKinds, SEPARATOR);

        EntityNamedNoteTypeRecord record = dsl.newRecord(ennt);
        record.setName(name);
        record.setExternalId(command.externalId().orElse(""));
        record.setDescription(command.description().orElse(""));
        record.setApplicableEntityKinds(kinds);
        record.setIsReadonly(command.isReadOnly().orElse(false));
        record.setPosition(command.position().orElse(0));

        record.store();

        return record.getId();
    }


    public boolean update(long id, EntityNamedNoteTypeChangeCommand command) {
        EntityNamedNoteTypeRecord record = new EntityNamedNoteTypeRecord();
        record.set(ennt.ID, id);
        record.changed(ennt.ID, false);

        command.name()
                .ifPresent(record::setName);
        command.externalId()
                .ifPresent(record::setExternalId);
        command.description()
                .ifPresent(record::setDescription);
        command.applicableEntityKinds()
                .ifPresent(kinds -> record.setApplicableEntityKinds(join(kinds, SEPARATOR)));
        command.isReadOnly()
                .ifPresent(record::setIsReadonly);
        command.position()
                .ifPresent(record::setPosition);

        return dsl.executeUpdate(record) == 1;
    }


    public EntityNamedNodeType getById(long namedNoteTypeId) {
        return dsl.select(ennt.fields())
                .from(ennt)
                .where(ennt.ID.eq(namedNoteTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public EntityNamedNodeType getByExternalId(String externalId) {
        return dsl
                .select(ennt.fields())
                .from(ennt)
                .where(ennt.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<EntityWithOperations<EntityNamedNodeType>> findForRefAndUser(EntityReference ref,
                                                                            String username) {

        SystemRole requiredRole = getRequiredRoleForEntityKind(ref.kind());

        SelectConditionStep<Record> qry = dsl
                .select(ennt.fields())
                .select(enn.LAST_UPDATED_AT) // already has a note ?
                .select(ur.USER_NAME)  // user has role ?
                .select(agm.USER_ID) // user is part of app group
                .from(ennt)
                .leftJoin(enn)
                .on(enn.NAMED_NOTE_TYPE_ID.eq(ennt.ID)
                        .and(enn.ENTITY_ID.eq(ref.id()))
                        .and(enn.ENTITY_KIND.eq(ref.kind().name())))
                .leftJoin(ur)
                .on(ur.USER_NAME.eq(username)
                        .and(ur.ROLE.eq(requiredRole.name())))
                .leftJoin(agm)
                .on(ref.kind() == EntityKind.APP_GROUP
                            ? DSL.trueCondition()
                            : DSL.falseCondition())
                        .and(agm.GROUP_ID.eq(ref.id()))
                        .and(agm.USER_ID.eq(username))
                        .and(agm.ROLE.eq(AppGroupMemberRole.OWNER.name()))
                .where(ennt.APPLICABLE_ENTITY_KINDS.like(String.format("%%%s%%", ref.kind())));

        return qry
                .fetch()
                .stream()
                .map(r -> {
                    EntityNamedNodeType entityNamedNodeType = TO_DOMAIN_MAPPER.map(r);
                    boolean alreadyExists = r.get(enn.LAST_UPDATED_AT) != null;
                    boolean hasBasicRole = r.get(ur.USER_NAME) != null;
                    boolean isAppGroup = ref.kind() == EntityKind.APP_GROUP;
                    boolean hasAppGroupOwnership = r.get(agm.USER_ID) != null;

                    Set<Operation> ops = asSet(
                            Operation.ADD,
                            Operation.UPDATE,
                            Operation.REMOVE);

                    if (entityNamedNodeType.isReadOnly()) {
                        ops = new HashSet<>();
                    }

                    if (alreadyExists) {
                        ops = minus(ops, asSet(Operation.ADD));
                    } else {
                        ops = minus(ops, asSet(Operation.UPDATE, Operation.REMOVE));
                    }

                    if (isAppGroup && ! hasAppGroupOwnership) {
                        ops = new HashSet<>();
                    }

                    if (!isAppGroup && !hasBasicRole) {
                        ops = new HashSet<>();
                    }

                    return ImmutableEntityWithOperations
                            .<EntityNamedNodeType>builder()
                            .entity(entityNamedNodeType)
                            .addAllOperations(ops)
                            .build();
                })
                .collect(Collectors.toSet());

    }
}
