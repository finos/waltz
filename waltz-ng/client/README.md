

# OAuth (Single Sign-On) Implementation

## Overview

The following document outlines *a* solution for integrating OAuth Single Sign-On. It may not be applicable for integrating to all OAuth providers.

Some of the changes are integrated into the Waltz release and are fully-compatible with either ```waltz``` or ```sso``` as the ```web.authentication``` method (see [settings](../../docs/features/configuration/settings.md)).
Other options need to be integrated to your build of Waltz once you have committed to ```sso```. Guidance for those changes is provided below.

With this implementation, the users *should* get created automatically. You may want to also assign default user roles via the 'Settings' database table or overrides (see [settings](../../docs/features/configuration/settings.md)).

## Impacted files

Changes are required, unless specifically labeled as 'Optional' or 'Optional, but recommended'

* waltz-ng/client/common/[WaltzHttp.js](./common/WaltzHttp.js)
* waltz-ng/client/navbar/[svelte-page.html](./navbar/svelte-page.html)
* waltz-ng/client/navbar/[svelte-page.js](./navbar/svelte-page.js)
* waltz-ng/client/svelte-stores/[remote.js](./svelte-stores/remote.js)
* waltz-ng/client/[thirdparty-setup.js](./thirdparty-setup.js)
* waltz-web/src/main/java/org/finos/waltz/web/endpoints/auth/[AuthenticationEndpoint.java](../../waltz-web/src/main/java/org/finos/waltz/web/endpoints/auth/AuthenticationEndpoint.java)


## (Optional)  Implementation: WaltzHttp.js

**Summary:** (Optional, but recommended) There are 4 functions in this file that should be modified to enable exception handling for the token:
* function get(url)
* function post(url, body) 
* function put(url, body)
* function _delete(url)

Exception Handling - if there is an issue with the "satellizer_token" (ex. failed signature verification, malformed json, etc), then revoke the token and reload the page.

**Setup:** Replace the `.then(handleResponse);` line with exception handling block. The snipped below should be implemented for all 4 functions.

	function get(url) {
		const requestOptions = {
			method: "GET",
			headers
		};
		return fetch(url, requestOptions)
			.then(handleResponse)
			.catch(e =>{
				if(e.error && (e.error.includes('SignatureVerificationException')
					|| e.error.includes('JWTDecodeException'))){
					localStorage.removeItem("satellizer_token");
					window.location.reload();					
				}
				throw e;	
			});
	}



## (Optional) Implementation: svelte-page.html

**Summary:** (Optional) Add conditional logic to block the page from rendering if the user isn't Authenticated. Some organizations may want to allow for Anonymous browsing for all, but this option would add another layer of protection. 

**Setup:** In conjuction with changes to `svelte-page.js`, you can create flags/functions to check if 1) Authentication Failed and 2) User Authenticated Successfully

Example definitions:

	isAuthenticated()
		True: user has been Authenticated successfully via a thirdparty OAuth provider
		False: Authentication was not successful
		(Default) undefined: function has not executed yet
		
	isAuthFailed
		True: Authentication was attempted and failed
		(Default) False: Authentication attempt not yet initiated or still in progress (success or failure not yet determined)
		
		
Then you can wrap the entire Header and Content section in a `<div>` and then add two conditional messages to display Authentication progress/status...

	<div ng-if="isAuthenticated()">
		<!-- Header -->
		<div ui-view="header">
		
			<!-- original source code continues here -->
	
			</main>
		</div>
	</div>
	<div ng-if="!isAuthenticated() && !isAuthFailed">
		<h2>Authenticating user...</h2>	
	</div>
	<div ng-if="!isAuthenticated() && isAuthFailed">
		<h2>Authentication Failed</h2>	
		<h3>Contact your Waltz System Administrator for more information</h3>	
	</div>
	


## Implementation: svelte-page.js

**Summary:** This section calls the satellizer implementation of the OAuth Provider and attempts to authenticate the user. Conditional logic will set flags to indicate authentication status.

**Setup:** Some elements have been added to support this function, but other elements need to be added in if ```sso``` through a thirdparty OAuth provider is the desired outcome. 


*IMPORTANT:* Some of the required changes may not be compatible with ```waltz``` as the ```web.authentication``` method. Therefore, they are not included in the Waltz release. Provided below is a sample to be added within the `vm.$onInit` lifecycle hook if ```sso``` is enabled. This will initiate the OAuth process via the `$auth.authenticate("oauthprovider")` call:

	// try to authenticate with OAuth Provider
	if (!$auth.isAuthenticated()){
		$auth.authenticate("oauthprovider")
		.then(function(response){
			console.log("authentication through oauthprovider - Successful");
			window.location.reload();
		})
		.catch(function(response){
			console.log("authentication through oauthprovider - FAILED");
			$scope.isAuthFailed = true;
			return false;
		});
	}

* Note: "oauthprovider" in the snippet above needs to match the 'name' you provide in thirdparty-setup.js


## (Optional) Implementation: remote.js

**Summary:** (Optional, but recommended) There is 1 function in this file that should be modified to enhance exception handling for the token:
* function _fetchData(cache, method, url, data, init = [], config = { force: false })

Exception Handling - if there is an issue with the "satellizer_token" (ex. failed signature verification, malformed json, etc), then revoke the token and reload the page.

**Setup:** Replace the `.catch(e => cache.err(key, e, init));` line with exception handling block. The snipped below should be implemented for the fetchData function.


	function _fetchData(cache, method, url, data, init = [], config = { force: false }) {
		const key = mkKey(method, url, data);
		const forcing = _.get(config, ["force"], false);

		const invokeFetch = () => mkPromise(method, url, data)
			.then(r => cache.set(key, r.data))
			.catch(e => { cache.err(key, e, init))
					if(e.error && (e.error.includes('SignatureVerificationException')
					|| e.error.includes('JWTDecodeException')
					|| e.error.includes('invalid_token'))){
						localStorage.removeItem("satellizer_token");
						window.location.reload();						
					}			
			}

		if (cache.has(key)) {
			if (forcing) {
				invokeFetch();
			}
		} else {
			cache.init(key, init);
			invokeFetch();
		}

		return cache.get(key);
	}



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


---

* Java 8
* Maven 3.5
* Tomcat 8 (optional)

## Building

To build the web server use:

    $> mvn clean package  [-DskipTests]

# Configuration

Currently looks on classpath for `waltz.properties` or falls
back to `<home>/.waltz/waltz.properties` for options, should look something like this:
[waltz.properties](example.waltz.properties.md)

# Running

## Without container (using uber jar)

Launch the server with

    $> cd waltz-web/target
    $> java -cp uber-waltz-web-1.0-SNAPSHOT.jar Main


## With container (i.e. Tomcat)

Deploy the war file in:

    waltz-web/target/waltz-web.war

Ensure `waltz.properties` and and overridden `logback.xml` file
are available (typically on the classpath).

## Both

When the server starts you will see messages about registering
endpoints and CORS services, similar to:

````
....
7:59:43.633 [main] INFO  Main - Registering Endpoint: userEndpoint
17:59:43.637 [main] INFO  Main - Registering Endpoint: authenticationEndpoint
17:59:43.639 [main] INFO  Main - Registering Endpoint: dataExtractEndpoint
17:59:43.643 [main] DEBUG c.k.w.w.e.a.StaticResourcesEndpoint - Registering static resources
17:59:43.644 [main] INFO  Main - Completed endpoint registration
17:59:43.649 [main] INFO  Main - Enabled CORS
````



