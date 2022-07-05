package org.finos.waltz.web.endpoints.extracts;

import org.immutables.value.Value;
import org.jooq.Select;

/*
Define the requirements for the extract
 */
@Value.Immutable
public interface ExtractSpecification {

    ExtractFormat extractFormat();

    /* A file name or longer name for this extract*/
    String outputName();

    Select<?> qry();
}
