# External Authentication

Waltz can delegate authentication to an external end-point if required.
This can be used to integrating with an existing auth system (eg: LDAP/Active Directory).

## Steps

#### 1. Define and deploy a custom end-point to authenticate users

* **HTTP method**: ```POST```
* **Input**: Map/POJO containing ```userName``` and ``password``. Waltz will forward the values entered in the UI to the external end-point
* **Output**: Map/POJO containing:
  * ```success```: Boolean, flag indicating if authentication was successful
  * ```waltzUserName```: String, representing this user's user-name in Waltz (usually the user's email address). This is useful if the login user-name in the external auth system is different to email addresses
  * ```errorMessage```: String, error message in case of auth failure  

###### Example end-point snippet for LDAP authentication (deployed using a Spring Boot app):

```
@PostMapping("/authenticate-user")
public Map<String, Object> authenticateUser(@RequestBody Map<String, String> params) {
    Map<String, Object> responseMap = new HashMap<>();

    String userName = params.get("userName");
    String password = params.get("password");

    DirContext context = null;
    try {
        context = ldapContextSource.getContext(mkUserPrincipal(userName), password);
        DirContextAdapter lookup = (DirContextAdapter)context.lookup(mkLookupName(userName));
        String email = lookup.getStringAttribute("mail");

        responseMap.put("success", true);
        responseMap.put("waltzUserName", email.toLowerCase());
        responseMap.put("errorMessage", null);
    } catch (Exception e) {
        responseMap.put("success", false);
        responseMap.put("errorMessage", mkErrorMessage(e.getMessage()));
    } finally {
        LdapUtils.closeContext(context);
    }

    return responseMap;
}
```

#### 2. Configure Waltz to use this custom end-point for authentication
Set the following [setting](../../features/configuration/settings.md):

```server.authentication.external.endpoint.url``` e.g. ```http://localhost:8080/authenticate-user``` 