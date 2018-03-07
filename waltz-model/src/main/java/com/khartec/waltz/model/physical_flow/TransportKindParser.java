package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumParser;

public class TransportKindParser extends EnumParser<TransportKind> {

    private static final Aliases<TransportKind> defaultAliases = new Aliases<>()
            .register(TransportKind.EMAIL, "SMTP")
            .register(TransportKind.DATABASE_CONNECTION, "OLEDB", "JDBC", "DB", "DB-LINK", "DIRECT DATABASE ACCESS")
            .register(TransportKind.FILE_TRANSPORT, "SFTP", "SFTPS", "FTP", "SCP", "FILE")
            .register(TransportKind.FILE_SHARE, "SAMBA")
            .register(TransportKind.MESSAGING, "JMS")
            .register(TransportKind.OTHER, "OTHER", "N/A", "NA")
            .register(TransportKind.WEB, "HTTP", "HTTPS", "SOAP");


    public TransportKindParser() {
        super(defaultAliases, TransportKind.class);
    }
}