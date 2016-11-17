package com.khartec.waltz.model.physical_specification;

/**
 * How an specification manifests itself 'physically'.
 * <p/>
 * The UNKNOWN option is not intended to be exposed as
 * as selectable choice for users.  It is intended to be
 * used when bulk importing from systems which do not have
 * any equivalent format representation
 */
public enum DataFormatKind {

    BINARY,
    DATABASE,
    FLAT_FILE,
    JSON,
    OTHER,
    UNSTRUCTURED,
    UNKNOWN,
    XML

}
