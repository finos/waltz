

# OAuth (Single Sign-On) Implementation

## Overview

The following document outlines *a* solution for integrating OAuth Single Sign-On. It may not be applicable for integrating to all OAuth providers.

Some of the changes are integrated into the Waltz release and are fully-compatible with either ```waltz``` or ```sso``` as the ```web.authentication``` method (see [settings](../../docs/features/configuration/settings.md)).
Other options need to be integrated to your build of Waltz once you have committed to ```sso```. Guidance for those changes is provided below.

With this implementation, the users *should* get created automatically. You may want to also assign default user roles via the 'Settings' database table or overrides (see [settings](../../docs/features/configuration/settings.md)).

## Impacted files

Changes are required, unless specifically labeled as 'Optional' or 'Optional, but recommended'

* waltz-ng/client/navbar/[svelte-page.html](./navbar/svelte-page.html)
* waltz-ng/client/navbar/[svelte-page.js](./navbar/svelte-page.js)
* waltz-ng/client/[thirdparty-setup.js](./thirdparty-setup.js)
* waltz-web/src/main/java/org/finos/waltz/web/endpoints/auth/[AuthenticationEndpoint.java](../../waltz-web/src/main/java/org/finos/waltz/web/endpoints/auth/AuthenticationEndpoint.java)


## Implementation: svelte-page.html

**Summary:** Add conditional logic to block the page from rendering if the user isn't Authenticated. Some organizations may want to allow for Anonymous browsing for all, but this option would add another layer of protection. 

**Setup:** This logic can be enabled via setting ```oauth.disable.anonymous``` to ```true``` (see [settings](../../docs/features/configuration/settings.md)).


## Implementation: svelte-page.js

**Summary:** This section can call the satellizer implementation of the OAuth Provider and attempts to authenticate the user. Conditional logic will set flags to indicate authentication status.

**Setup:** There are the configurations that can be setup (see also [settings](../../docs/features/configuration/settings.md)).

* Waltz Login
	* ```web.authentication``` = ```waltz```
	* ```oauth.provider.name```  <-- do not add to Settings
	* ```oauth.disable.anonymous``` <-- do not add to Settings
* SSO (Externally Managed)
	* ```web.authentication``` = ```sso```
	* ```oauth.provider.name``` <-- do not add to Settings
	* ```oauth.disable.anonymous``` <-- do not add to Settings
* SSO (thirdparty integration with satellizer)
	* ```web.authentication``` = ```sso```
	* ```oauth.provider.name``` needs to match the 'name' you provide in thirdparty-setup.js
	* ```oauth.disable.anonymous``` can be set to ```true`` (blocks anonymous browsing) or ```false``` (allows anonymous browsing) or left out of Settings (allows anonymous browsing)

This can be enabled via setting ```oauth.disable.anonymous``` to ```true``` (see [settings](../../docs/features/configuration/settings.md)).


## Implementation: thirdparty-setup.js

**Summary:** This file defines the structure of the requests between Waltz and the thirdparty OAuth Provider. It's recommended that you setup the requests externally and confirm that they work before directly trying to integrate into Waltz.


**Setup:** Provided below is the framework for an implementation of a thirdparty OAuth Provider. Requirements and therefore, implementations, will vary based on your organizational setup.

    $authProvider.oauth2({
        name: 'oauthprovider',
        clientId: 'Waltz',
        url: 'authentication/oauth',
        authorizationEndpoint: '',
        redirectUri: '',
        optionalUrlParams: ['scope', 'save_consent', 'csrf', 'code_challenge', 'code_challenge_method'],
        scope: ['userid+email+profile+roles'],
        scopeDelimiter: ' ',
        saveConsent: '',
        csrf: '',
        codeChallenge: '',
        codeChallengeMethod: 'S256',
        oauthType: '2.0',
        responseType: 'code',
        popupOptions: { width: 600, height: 500 }
    });

Notes and Tips:
* A Postman collection could be provided to define and verify all of the header and body requirements for each OAuth request - recreate that format within the requests in Waltz
* The `url` option defines an endpoint we'll be referencing in AuthenticationEndpoint.java - these need to match
* The redirect URI could be generic `window.location.origin`, but this might not work as a 'valid' address for some providers i.e. you must specify the address
* The `optionalUrlParams` are listed on this line, and then assigned a value on the lines below - it's expected that these will vary by organization
* see https://github.com/sahat/satellizer for more details


## Implementation: AuthenticationEndpoint.java

**Summary:** This is the implementation of all of the requests between Waltz and the thirdparty OAuth Provider. This is only a framework for an implementation and will require additional work to integrate for your organization.

**Setup:** Setting up the endpoint and additional functionality for a thirdparty OAuth Provider. 

Notes and Tips:
* A Postman collection could be provided to define and verify all of the header and body requirements for each OAuth request - recreate that format within the requests in Waltz
* The `url: 'authentication/oauth'` in thirdparty-setup.js should match the name of the endpoint configured here `Spark.post(WebUtilities.mkPath(BASE_URL, "oauth"), (request, response) -> {`
* Additional user information may be accessible via your external thirdparty OAuth Provider. This implementation can be expanded to request, parse and utilize that info as needed
