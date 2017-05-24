package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class PhysicalSpecDataTypeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-data-type");

    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;


    @Autowired
    public PhysicalSpecDataTypeEndpoint(PhysicalSpecDataTypeService physicalSpecDataTypeService) {
        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
    }


    @Override
    public void register() {
        String findBySpecificationPath = mkPath(BASE_URL, "specification", ":id");
        String findBySpecificationSelectorPath = mkPath(BASE_URL, "specification", "selector");

        ListRoute<PhysicalSpecificationDataType> findBySpecificationRoute = (req, res)
                -> physicalSpecDataTypeService.findBySpecificationId(getId(req));

        ListRoute<PhysicalSpecificationDataType> findBySpecificationSelectorRoute = (req, res)
                -> physicalSpecDataTypeService.findBySpecificationIdSelector(readIdSelectionOptionsFromBody(req));

        getForList(findBySpecificationPath, findBySpecificationRoute);
        postForList(findBySpecificationSelectorPath, findBySpecificationSelectorRoute);
    }
}
