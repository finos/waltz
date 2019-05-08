package com.khartec.waltz.jobs.tools.exporters.archi;

import com.khartec.waltz.job.tools.exporters.archi.*;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityReferenceUtilities;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableApplication;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.rating.RagRating;

import javax.xml.bind.JAXB;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.model.EntityReferenceUtilities.basicFmt;

public class ArchiExport {

    public static final Function<Application, ApplicationService> TO_ARCHI = app -> {
        ApplicationService archi = new ApplicationService();
        archi.setIdentifier(basicFmt(app.entityReference()));
        archi.getNameGroup().add(mkStr(app.name()));
        PropertiesType props = new PropertiesType();
        List<PropertyType> propList = props.getProperty();
        PropertyType nameProp = new PropertyType();
        nameProp.setPropertyDefinitionRef(PropIds.APP_NAME);
        LangStringType nameStr = new LangStringType();
        nameStr.setValue(app.name());
        nameProp.getValue().add(nameStr);
        propList.add(nameProp);
        archi.setProperties(props);
        return archi;
    };


    private static LangStringType mkStr(String val) {
        LangStringType str = new LangStringType();
        str.setValue(val);
        return str;
    }


    public static void main(String[] args) {
        ApplicationService archi = mkApp();

        PropertyDefinitionsType defs = mkProps();

        ModelType model = new ModelType();
        model.setVersion("0.1");
        model.setPropertyDefinitions(defs);
        ElementsType elements = new ElementsType();
        elements.getElement().add(archi);
        model.setElements(elements);

        JAXB.marshal(model, System.out);
    }


    private static PropertyDefinitionsType mkProps() {
        PropertyDefinitionType namePropDef = new PropertyDefinitionType();
        namePropDef.setType(DataType.STRING);
        namePropDef.setIdentifier(PropIds.APP_NAME);

        namePropDef.getNameGroup().add(mkStr("name"));

        PropertyDefinitionsType defs = new PropertyDefinitionsType();
        defs.getPropertyDefinition().add(namePropDef);
        return defs;
    }


    private static ApplicationService mkApp() {
        Application app = ImmutableApplication.builder()
                .name("Test App")
                .id(1L)
                .applicationKind(ApplicationKind.IN_HOUSE)
                .overallRating(RagRating.G)
                .assetCode("1234-1")
                .businessCriticality(Criticality.HIGH)
                .lifecyclePhase(LifecyclePhase.PRODUCTION)
                .organisationalUnitId(12L)
                .build();

        return TO_ARCHI.apply(app);
    }
}
