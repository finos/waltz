package com.khartec.waltz.model;

public class EntityLinkUtilities {

    public static String mkIdLink(String baseUrl, EntityKind kind, Long id) {
        return baseUrl + "entity/" + kind.name() + "/id/" + id;
    }


    public static String mkExternalIdLink(String baseUrl, EntityKind kind, String externalId) {
        return baseUrl + "entity/" + kind.name() + "/external-id/" + externalId;
    }
}
