# Settings

These options are configured in the `settings` table.  Optionally they may
be overriden in the `waltz.properties` configuration by specifying a property 
name `settings.override` consisting of semicolon delimited, name value pairs.  
For example:

```
settings.override=ui.logo.overlay.text=Hello World;database.pool.max=20
```

## Presentation

* ```ui.logo.overlay.text``` - optional text to overlay on the logo, keep it short
* ```ui.logo.overlay.color``` - colour used to render the optional overlay on the logo
* ```ui.inactivity-timeout``` - time in miliseconds of inactivity before the UI triggers a page refresh, disabled if not provided
* ```ui.avatar.template.url``` - url template to use when rendering person avatar images. 
  * Example: `http://directory.intranet.mycomp.com/photo?empId=${employeeId}` - Available variables:
    * `employeeId` 
    * `id` - waltz internal id
    * `email`
  * Example 2: `https://gravatar.com/avatar/${id}?s=200&d=robohash&r=pg`

Use these settings to let users know if they are on a beta instance. 

* ```web.beta``` - is this instance of waltz running beta code (```true | false```)
* ```web.beta.nag-message``` - the message to display if non-beta user on a beta environment



## Security

Group of settings which control how waltz handles authentication/authorization

* ```web.authentication```
  * ```waltz``` - indicates that waltz is handling authentication, the login panel will be displayed
  * ```sso``` - authentication is done elsewhere, do not show the login panel
* ```oauth.provider.name``` <-- optional
  * this is the name of the oauth2 provider set up in ```oauth.provider.name```
  * By default, this is `null`
* ```oauth.provider.details``` <-- optional
  * JSON containing thirdparty OAuth Provider details  (see [README](../../../waltz-ng/client/README.md)).
  * By default, this is `{name: null}`
* ```oauth.disable.anonymous``` <-- optional
  * ```true``` - this will prevent users from viewing Waltz if they are not authenticated via ```sso```
  * ```false``` - [default] this allows Anonymous browsing
* ```server.authentication.filter```
  * ```<classname>``` - the name of the class which injects the user object into incoming requests.
    By default this is ```JWTAuthenticationFilter```, options are:
    * ```HeaderBasedAuthenticationFilter```
    * ```JWTAuthenticationFilter```
* ```server.authentication.roles.default``` - Default set of role names (comma sep)

If using header based authentication provide an additional setting which gives the name of the parameter to obtain the username from:

* `server.authentication.filter.headerbased.param`  e.g. `ct_user`

If using [external authentication](../../integration/external-authentication/external-authentication.md), provide an additional setting which gives the external URL to handle authentication:

* `server.authentication.external.endpoint.url`  e.g. `http://localhost:8080/authenticate-user`

## Development (```web.devext``` )

Development options can be used by developers to aid in writing code for Waltz

* ```web.devext.enabled``` - master switch, if enabled other settings in this group will be activated
* ```web.devext.http.header.*``` - values in this group will be added to the header of
    all $http calls (primarily api calls)


## Features (```feature.*```)

Features can be enabled/disabled and configured via settings.  Available options are:

* ```feature.data-extractor.entity-cost.enabled```
    * (**`true`**|`false`) allow users to export asset costs (defaults to true)
* ```feature.measurable-rating-roadmaps.enabled```
    * (**`true`**|`false`) display links to roadmap pages (defaults to true)
   


## General Settings (```settings.*```)

* ```settings.asset-cost.default-currency```
    * currency code, defaults to `EUR`

## Defaults

General default settings

* ```settings.data-type.default-code``` - `DEPRECATED - replaced by 'unknown' flag is data_type table`
    * (```UNKNOWN```) UNKNOWN is the code for the default data type
* ```settings.data-type.unknown-id``` - `DEPRECATED - replaced by 'unknown' flag is data_type table`
    * (```<id>```) The id of the unknown data type (optional) 
* ```settings.measurable.default-category```
    * (```<id>```) The id of the default category to show when looking a 'Other Viewpoints'  (optional)
* ```server.gzip.enabled```
    * (```true|false```) enable or disable gzipping of content
* ```server.gzip.minimum-size```
    * (```8192```) the size of the response before gzip is enabled
