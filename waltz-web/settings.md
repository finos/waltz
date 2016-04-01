
# Settings

## Security

* ```web.authentication```
  * ```waltz``` - indicates that waltz is handling authentication, the login panel will be displayed
  * ```sso``` - authentication is done elsewhere, do not show the login panel

* ```server.authentication.filter```
  * ```<classname>``` - the name of the class which injects the user object into incoming requests.
    By default this is ```com.khartec.waltz.web.endpoints.auth.JWTAuthenticationFilter```

