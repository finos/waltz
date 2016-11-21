package com.khartec.waltz.model.physical_flow;

/**
 * Represents how a data flow is enacted between systems.
 * <p/>
 * The UNKNOWN option is not intended to be exposed as
 * as selectable choice for users.  It is intended to be
 * used when bulk importing from systems which do not have
 * any equivalent transport representation
 */
public enum TransportKind {

    DATABASE_CONNECTION,
    EMAIL,
    FILE_SHARE,
    FILE_TRANSPORT,
    MESSAGING,
    OTHER,
    RPC,
    UNKNOWN,
    UDP,
    WEB

}
