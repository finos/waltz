/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.auth;


import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.khartec.waltz.common.EnumUtilities;
import com.khartec.waltz.common.MapBuilder;
import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.LoginRequest;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.before;
import static spark.Spark.post;


@Service
public class AuthEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("auth");
    private static final String SECRET = "secret";

    private static final User ANONYMOUS_USER = ImmutableUser.builder()
            .userName("anonymous")
            .addRoles(Role.ANONYMOUS)
            .build();

    private final UserService userService;
    private final UserRoleService userRoleService;


    @Autowired
    public AuthEndpoint(UserService userService,
                        UserRoleService userRoleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        post(mkPath(BASE_URL, "login"), (request, response) -> {

            LoginRequest login = readBody(request, LoginRequest.class);

            if (userService.authenticate(login)) {

                List<Role> roles = userRoleService.getUserRoles(login.userName());

                Map<String, Object> claims = new MapBuilder()
                        .add("iss", "Waltz")
                        .add("sub", login.userName())
                        .add("roles", roles)
                        .add("displayName", login.userName())
                        .add("employeeId", login.userName())
                        .build();

                JWTSigner signer = new JWTSigner(SECRET);
                String token = signer.sign(claims);

                return newHashMap("token", token);
            } else {
                response.status(401);

                return "Unknown user/password";
            }
        }, transformer);

        before(mkPath("api", "*"), ((request, response) -> {
            JWTVerifier verifier = new JWTVerifier(SECRET);
            String authorizationHeader = request.headers("Authorization");

            if (authorizationHeader == null) {
                request.attribute("waltz-user", ANONYMOUS_USER);
            } else {
                String token = authorizationHeader.replaceFirst("Bearer ", "");
                Map<String, Object> claims = verifier.verify(token);

                Set<Role> roles = ((List<String>) claims.get("roles"))
                        .stream()
                        .map(r -> EnumUtilities.readEnum(r, Role.class, null))
                        .filter(r -> r != null)
                        .collect(Collectors.toSet());

                request.attribute("user", ImmutableUser.builder()
                        .userName((String) claims.get("sub"))
                        .roles(roles)
                        .build());
            }
        }));

    }

}
