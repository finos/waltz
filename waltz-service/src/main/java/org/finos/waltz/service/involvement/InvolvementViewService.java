package org.finos.waltz.service.involvement;


import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.involvement.InvolvementViewDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyDirection;
import org.finos.waltz.model.utils.IdUtilities;
import org.finos.waltz.service.involvement_kind.InvolvementKindService;
import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.involvement.*;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;

@Service
public class InvolvementViewService {

    private final InvolvementService involvementService;
    private final InvolvementKindService involvementKindService;
    private final PersonService personService;
    private final InvolvementViewDao involvementViewDao;

    @Autowired
    public InvolvementViewService(InvolvementService involvementService,
                                  InvolvementKindService involvementKindService,
                                  PersonService personService,
                                  InvolvementViewDao involvementViewDao) {

        this.involvementService = involvementService;
        this.involvementKindService = involvementKindService;
        this.personService = personService;
        this.involvementViewDao = involvementViewDao;
    }


    public Set<InvolvementViewItem> findAllByEmployeeId(String employeeId) {

        List<Involvement> involvements = involvementService.findAllByEmployeeId(employeeId);

        Set<String> employeeIds = map(involvements, Involvement::employeeId);
        Set<Person> involvedPeople = personService.findByEmployeeIds(employeeIds);
        Map<String, Person> peopleByEmployeeId = indexBy(involvedPeople, Person::employeeId);

        return involvements
                .stream()
                .map(d -> {
                    Person person = peopleByEmployeeId.getOrDefault(d.employeeId(), null);

                    if (person == null) {
                        return null;
                    }

                    return mkInvolvementViewItem(d, person);
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }


    public Set<InvolvementDetail> findKeyInvolvementsForEntity(EntityReference ref) {

        List<InvolvementKind> keyInvolvements = involvementKindService.findKeyInvolvementKindsByEntityKind(ref.kind());
        Map<Long, InvolvementKind> keyInvKindsById = IdUtilities.indexByOptionalId(keyInvolvements);

        List<Involvement> involvements = involvementService.findByEntityReference(ref);
        List<Person> people = involvementService.findPeopleByEntityReference(ref);
        Map<String, Person> peopleByEmployeeId = indexBy(people, Person::employeeId);

        return involvements
                .stream()
                .filter(d -> keyInvKindsById.containsKey(d.kindId()))
                .map(d -> ImmutableInvolvementDetail.builder()
                        .involvement(d)
                        .involvementKind(keyInvKindsById.get(d.kindId()))
                        .person(peopleByEmployeeId.get(d.employeeId()))
                        .build())
                .filter(d -> ! d.person().isRemoved())
                .collect(Collectors.toSet());
    }


    private InvolvementViewItem mkInvolvementViewItem(Involvement involvement,
                                                      Person person) {
        return ImmutableInvolvementViewItem.builder()
                .involvement(involvement)
                .person(person)
                .build();
    }

    public Set<InvolvementViewItem> findByKindIdAndEntityKind(long id,
                                                              EntityKind kind) {
        Set<Involvement> involvements = involvementService.findByKindIdAndEntityKind(id, kind);

        Set<String> employeeIds = map(involvements, Involvement::employeeId);
        Set<Person> involvedPeople = personService.findByEmployeeIds(employeeIds);
        Map<String, Person> peopleByEmployeeId = indexBy(involvedPeople, Person::employeeId);

        return involvements
                .stream()
                .map(i -> {
                    Person person = peopleByEmployeeId.get(i.employeeId());

                    if (person == null) {
                        return null;
                    } else {
                        return mkInvolvementViewItem(i, person);
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }


    public InvolvementDetailByDirectionResults getAllInvolvements(EntityReference ref) {
        Map<HierarchyDirection, ? extends Collection<InvolvementDetail>> byDirection = involvementViewDao.findAllInvolvements(ref);

        return ImmutableInvolvementDetailByDirectionResults
                .builder()
                .ancestors(SetUtilities.fromCollection(byDirection.get(HierarchyDirection.ANCESTOR)))
                .descendents(SetUtilities.fromCollection(byDirection.get(HierarchyDirection.DESCENDENT)))
                .exact(SetUtilities.fromCollection(byDirection.get(HierarchyDirection.EXACT)))
                .build();
    }

}
