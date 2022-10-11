package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.*;
import org.finos.waltz.data.EntityAliasPopulator;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.bulk_upload.ImmutableResolveRowResponse;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolveBulkUploadRequestParameters;
import org.finos.waltz.model.bulk_upload.ResolveRowResponse;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.person.PersonService;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.finos.waltz.common.ArrayUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkUploadService {

    public static final int REQUIRED_INVOLVEMENT_COLUMNS_SIZE = 2;
    private final GenericSelectorFactory genericSelector = new GenericSelectorFactory();
    private final PersonService personService;
    private final InvolvementService involvementService;
    private final EntityAliasPopulator entityAliasPopulator;

    @Autowired
    public BulkUploadService(PersonService personService,
                             InvolvementService involvementService,
                             EntityAliasPopulator entityAliasPopulator) {
        this.personService = personService;
        this.involvementService = involvementService;
        this.entityAliasPopulator = entityAliasPopulator;
    }

    public List<ResolveRowResponse> resolve(ResolveBulkUploadRequestParameters resolveParams) {

        switch (resolveParams.targetDomain().kind()) {
            case INVOLVEMENT_KIND:
                return resolveInvolvements(resolveParams);
            default:
                throw new IllegalArgumentException(format("Cannot resolve input rows for domain: %s", resolveParams.targetDomain().kind().name()));
        }
    }

    private List<ResolveRowResponse> resolveInvolvements(ResolveBulkUploadRequestParameters resolveParams) {

        Set<Tuple2<Long, Long>> existingInvolvements = involvementService
                .findEntityIdToPersonIdByInvolvementKindAndEntityKind(resolveParams.targetDomain().id(), resolveParams.rowSubjectKind());

        Set<String> subjectIdentifiers = getColumnValuesFromInputString(resolveParams.inputString(), 0);
        Map<String, Long> subjectIdentifierToIdMap = entityAliasPopulator.fetchEntityIdLookupMap(resolveParams.rowSubjectKind(), subjectIdentifiers);

        Set<String> personIdentifiers = getColumnValuesFromInputString(resolveParams.inputString(), 1);
        Map<String, Long> personIdentifierToIdMap = entityAliasPopulator.fetchEntityIdLookupMap(EntityKind.PERSON, personIdentifiers);

        return streamRowData(resolveParams.inputString())
                .map(t -> {

                    Integer lineNumber = t.v1;
                    String[] cells = t.v2;
                    List<String> rowData = asList(cells);

                    if (cells.length < REQUIRED_INVOLVEMENT_COLUMNS_SIZE) {
                        return mkErrorResponse(rowData, "Insufficient columns provided, ensure you have used the correct delimiter and have columns [external_id, email]");
                    }

                    String entityIdentifierString = safeTrim(cells[0]);
                    String personIdentifierString = safeTrim(cells[1]);

                    Long subjectId = subjectIdentifierToIdMap.get(entityIdentifierString);
                    Long personId = personIdentifierToIdMap.get(personIdentifierString);

                    if (subjectId == null) {
                        return mkErrorResponse(rowData, format("Subject: '%s' cannot be resolved", entityIdentifierString));
                    } else if (personId == null) {
                        return mkErrorResponse(rowData, format("Person: '%s' cannot be resolved, ensure user is active", personIdentifierString));
                    } else {

                        boolean existsAlready = existingInvolvements.contains(tuple(subjectId, personId));

                        return ImmutableResolveRowResponse.builder()
                                .inputRow(rowData)
                                .status(existsAlready ? ResolutionStatus.EXISTING : ResolutionStatus.NEW)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    private Set<String> getColumnValuesFromInputString(String inputString, int columnOffset) {
        return streamRowData(inputString)
                .filter(Objects::nonNull)
                .filter(t -> !isEmpty(t.v2))
                .filter(t -> t.v2.length > columnOffset)
                .map(t -> {
                    String[] cells = t.v2();
                    String cell = cells[columnOffset];
                    return safeTrim(cell);
                })
                .filter(StringUtilities::notEmpty)
                .collect(Collectors.toSet());
    }

    private Stream<Tuple2<Integer, String[]>> streamRowData(String inputString) {

        AtomicInteger lineNumber = new AtomicInteger(1);

        return IOUtilities.streamLines(new ByteArrayInputStream(inputString.getBytes()))
                .filter(StringUtilities::notEmpty)
                .filter(r -> !r.startsWith("#"))
                .map(r -> {
                    String delimiters = "[,;\\t|]+";
                    return r.split(delimiters);
                })
                .map(r -> tuple(lineNumber.getAndIncrement(), r));
    }

    private ResolveRowResponse mkErrorResponse(List<String> rowData, String errorMessage) {
        return ImmutableResolveRowResponse.builder()
                .inputRow(rowData)
                .status(ResolutionStatus.ERROR)
                .errorMessage(errorMessage)
                .build();
    }

}
