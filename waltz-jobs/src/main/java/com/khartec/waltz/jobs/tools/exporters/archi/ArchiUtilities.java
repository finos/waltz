package com.khartec.waltz.jobs.tools.exporters.archi;

import com.khartec.waltz.job.tools.exporters.archi.*;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.logical_flow.LogicalFlow;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.model.EntityReference.mkRef;

public class ArchiUtilities {

    public static ApplicationComponent toArchi(Application app) {
        ApplicationComponent archi = new ApplicationComponent();
        archi.setIdentifier(fmt(app.entityReference()));
        archi.getNameGroup().add(mkStr(app.name()));
        archi.getDocumentation().add(mkPreservedStr(app.description()));

        PropertiesType props = new PropertiesType();
        archi.setProperties(props);

        List<PropertyType> propList = props.getProperty();
        addStrProp(propList, WaltzArchiProperties.NAME, app.name());
        addStrProp(propList, WaltzArchiProperties.EXTERNAL_ID, app.assetCode());
        addStrProp(propList, WaltzArchiProperties.APP_TYPE, app.applicationKind().name());
        addStrProp(propList, WaltzArchiProperties.APP_CRITICALITY, app.businessCriticality().name());
        addStrProp(propList, WaltzArchiProperties.LIFECYCLE_PHASE, app.lifecyclePhase().name());

        return archi;
    }

    private static PreservedLangStringType mkPreservedStr(String value) {
        PreservedLangStringType str = new PreservedLangStringType();
        str.setValue(value);
        return str;
    }


    public static ApplicationComponent refToArchiApp(EntityReference ref) {
        String name = ref.name().orElse("?");

        ApplicationComponent archi = new ApplicationComponent();
        archi.setIdentifier(fmt(ref));
        archi.getNameGroup().add(mkStr(name));

        PropertiesType props = new PropertiesType();
        archi.setProperties(props);

        List<PropertyType> propList = props.getProperty();
        addStrProp(propList, WaltzArchiProperties.NAME, name);
        return archi;
    }


    public static Flow toArchi(LogicalFlow flow,
                               Map<EntityReference, ? extends RealElementType> appComponentsByRef) {
        Flow archi = new Flow();
        archi.setIdentifier(fmt(flow.entityReference()));
        archi.getNameGroup().add(mkStr(fmt(flow.entityReference())));
        archi.setSource(appComponentsByRef.get(flow.source()));
        archi.setTarget(appComponentsByRef.get(flow.target()));
        return archi;
    }


    public static PropertyDefinitionType mkStrPropDef(String id, String name) {
        PropertyDefinitionType namePropDef = new PropertyDefinitionType();
        namePropDef.setType(DataType.STRING);
        namePropDef.setIdentifier(id);
        namePropDef.getNameGroup().add(mkStr(name));
        return namePropDef;
    }


    public static LangStringType mkStr(String val) {
        LangStringType str = new LangStringType();
        str.setValue(val);
        return str;
    }


    public static PropertyType mkStrProp(PropertyDefinitionType propDef, String value) {
        PropertyType nameProp = new PropertyType();
        nameProp.setPropertyDefinitionRef(propDef);
        nameProp.getValue().add(mkStr(value));
        return nameProp;
    }


    public static PropertyType addStrProp(List<PropertyType> propList,
                                          PropertyDefinitionType propDef,
                                          Optional<String> value) {
        return value
                .map(v -> addStrProp(propList, propDef, v))
                .orElse(null);
    }


    public static PropertyType addStrProp(List<PropertyType> propList, PropertyDefinitionType propDef, String value) {
        PropertyType prop = mkStrProp(propDef, value);
        propList.add(prop);
        return prop;
    }


    /**
     * formatted NCNames must only contain: alphanumerics, periods, underscores and hyphens
     *
     * @param entityReference  entity ref to represent as a string
     * @return formatted entity ref
     */
    public static String fmt(EntityReference entityReference) {
        return String.format("%s.%d", entityReference.kind().name(), entityReference.id());
    }


    public static PropertyDefinitionsType mkPropDefinitions() {
        PropertyDefinitionsType defs = new PropertyDefinitionsType();
        List<PropertyDefinitionType> defList = defs.getPropertyDefinition();
        defList.add(WaltzArchiProperties.NAME);
        defList.add(WaltzArchiProperties.EXTERNAL_ID);
        defList.add(WaltzArchiProperties.APP_TYPE);
        defList.add(WaltzArchiProperties.APP_CRITICALITY);
        defList.add(WaltzArchiProperties.LIFECYCLE_PHASE);
        return defs;
    }


    public static ModelType mkModel(String identifier, String name) {
        ModelType model = new ModelType();
        model.setVersion("0.1");
        model.setIdentifier(identifier);
        model.getNameGroup().add(mkStr(name));

        model.setPropertyDefinitions(mkPropDefinitions());
        return model;
    }

}
