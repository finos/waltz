package com.khartec.waltz.service.involvement;


import com.khartec.waltz.model.involvement.ImmutableInvolvementViewItem;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.involvement.InvolvementViewItem;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.person.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;
import static java.util.stream.Collectors.toSet;

@Service
public class InvolvementViewService {

    private final InvolvementService involvementService;
    private final PersonService personService;

    @Autowired
    public InvolvementViewService(InvolvementService involvementService,
                                  PersonService personService){

        this.involvementService = involvementService;
        this.personService = personService;
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

                    if (person == null) {return null;}

                    return mkInvolvementViewItem(d, person);
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }


    private InvolvementViewItem mkInvolvementViewItem(Involvement involvement, Person person) {
        return ImmutableInvolvementViewItem.builder()
                .involvement(involvement)
                .person(person)
                .build();
    }
}
