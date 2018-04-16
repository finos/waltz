/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumUtilities;

import java.util.function.Function;

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
    MANUAL,
    MESSAGING,
    OTHER,
    RPC,
    UNKNOWN,
    UDP,
    WEB;


    private static final Aliases<TransportKind> defaultAliases = new Aliases<>()
            .register(TransportKind.EMAIL, "SMTP")
            .register(TransportKind.DATABASE_CONNECTION, "OLEDB", "JDBC", "DB", "DB-LINK", "DIRECT DATABASE ACCESS")
            .register(TransportKind.FILE_TRANSPORT, "SFTP", "SFTPS", "FTP", "SCP", "FILE")
            .register(TransportKind.FILE_SHARE, "SAMBA")
            .register(TransportKind.MANUAL, "MANUAL UPLOAD", "SNEAKERNET")
            .register(TransportKind.MESSAGING, "JMS")
            .register(TransportKind.OTHER, "OTHER", "N/A", "NA")
            .register(TransportKind.WEB, "HTTP", "HTTPS", "SOAP");


    public static TransportKind parse(String value, Function<String, TransportKind> failedParseSupplier) {
        return EnumUtilities.parseEnumWithAliases(value, TransportKind.class, failedParseSupplier, defaultAliases);
    }

}
