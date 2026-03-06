package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersonServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private PersonService personService;

    @Test
    public void testGetByUserEmails() {

        // 1. Arrange ----------------------------------------------------------
        personHelper.createPerson("test1@email.com");
        personHelper.createPerson("test2@email.com");

        // 2. Act --------------------------------------------------------------
        List<Person> result = personService.getByUserEmails(List.of("test1@email.com", "test2@email.com"));

        // 3. Assert -----------------------------------------------------------
        Assertions.assertEquals(2, result.size());

        List<Person> result2 = personService.getByUserEmails(List.of("test1@email.com"));
        Assertions.assertEquals(1, result2.size());

        List<Person> result3 = personService.getByUserEmails(emptyList());
        assertTrue(result3.isEmpty());
    }
}
