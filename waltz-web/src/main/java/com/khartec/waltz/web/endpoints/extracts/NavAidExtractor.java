package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.common.SvgUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.svg.SvgDiagram;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.service.svg.SvgDiagramService;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.StringUtilities.isNumericLong;
import static com.khartec.waltz.common.StringUtilities.toOptional;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.EntityReferenceUtilities.toUrl;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class NavAidExtractor extends BinaryDataBasedDataExtractor {

    @Value("${waltz.base.url}")
    private String baseUrl;

    private final DataTypeService dataTypeService;
    private final MeasurableService measurableService;
    private final OrganisationalUnitService organisationalUnitService;
    private final PersonService personService;
    private final SvgDiagramService svgDiagramService;


    @Autowired
    public NavAidExtractor(DataTypeService dataTypeService,
                           MeasurableService measurableService,
                           OrganisationalUnitService organisationalUnitService,
                           PersonService personService,
                           SvgDiagramService svgDiagramService) {
        this.dataTypeService = dataTypeService;
        this.measurableService = measurableService;
        this.organisationalUnitService = organisationalUnitService;
        this.personService = personService;
        this.svgDiagramService = svgDiagramService;
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "nav-aid", ":svgDiagramId");

        get(path, (request, response) -> {
            Long diagramId = getLong(request,"svgDiagramId");

            SvgDiagram diagram = svgDiagramService.getById(diagramId);
            String svgWithLinks = addHyperLinks(diagram);

            String suggestedFilename = diagram.name()
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            return writeExtract(
                    suggestedFilename,
                    svgWithLinks.getBytes(),
                    request,
                    response);
        });
    }


    private String addHyperLinks(SvgDiagram diagram) {
        Function<String, Optional<String>> keyToUrl = mkKeyToUrl(diagram.group());

        return Unchecked.supplier(() -> SvgUtilities.addWaltzEntityLinks(
                                            diagram.svg(),
                                            diagram.keyProperty(),
                                            keyToUrl))
                .get();
    }


    private Function<String, Optional<String>> mkKeyToUrl(String groupId) {
        if (groupId.startsWith("NAVAID.MEASURABLE.")) {
            String categoryIdStr = groupId.replace("NAVAID.MEASURABLE.", "");
            if (isNumericLong(categoryIdStr)) {
                return mkMeasurableKeyToUrl(Long.parseLong(categoryIdStr));
            }
        } else {
            switch (groupId) {
                case "DATA_TYPE":
                    return mkDataTypeKeyToUrl();
                case "ORG_UNIT":
                    return mkOrgUnitKeyToUrl();
                case "ORG_TREE":
                    return mkPersonKeyToUrl();
            }
        }

        return (key) -> Optional.empty();
    }


    private Function<String, Optional<String>> mkMeasurableKeyToUrl(Long categoryId) {
        Map<String, Long> extToIdMap = measurableService.findExternalIdToIdMapByCategoryId(categoryId);
        return (extId) -> Optional.ofNullable(extToIdMap.get(extId))
                                    .map(id -> toUrl(mkRef(EntityKind.MEASURABLE, id), baseUrl));
    }


    private Function<String, Optional<String>> mkDataTypeKeyToUrl() {
        Map<String, Long> codeToIdMap = dataTypeService.getCodeToIdMap();
        return (dtCode) -> Optional.ofNullable(codeToIdMap.get(dtCode))
                                    .map(id -> toUrl(mkRef(EntityKind.DATA_TYPE, id), baseUrl));
    }


    private Function<String, Optional<String>> mkOrgUnitKeyToUrl() {
        Set<Long> allIds = organisationalUnitService.findAllIds();
        return (unitId) -> toOptional(isNumericLong(unitId) && allIds.contains(Long.valueOf(unitId))
                                            ? toUrl(mkRef(EntityKind.ORG_UNIT, Long.valueOf(unitId)), baseUrl)
                                            : null);
    }


    private Function<String, Optional<String>> mkPersonKeyToUrl() {
        Map<String, Long> empIdToIdMap = personService.getEmployeeIdToIdMap();
        return (empId) -> Optional.ofNullable(empIdToIdMap.get(empId))
                                    .map(id -> toUrl(mkRef(EntityKind.PERSON, id), baseUrl));
    }
}
