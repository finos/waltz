# OAuth Setup

This is a guide to setting up OAuth authentication for Waltz, either using a standard provider or a custom provider.

### Providers:
Waltz supports OAuth authentication with the following providers:
* Facebook
* Google
* Github
* Instagram
* LinkedIn
* Twitter
* Twitch
* Microsoft Live
* Yahoo
* Bitbucket
* Spotify

Providers may need additional configuration to work correctly, such as updating the URL's for authentication. This can be configured in `thirdparty-setup.js`.


### Custom Providers:

To setup a custom provider you will need to configure satellizer to use the provider.
1. Navigate to `thirdparty-setup.js` and create a new config field.
2. The config field should be as follows:
```js
$authProvider.oauth2({
   name: "waltz",
   clientId: "  ",
   url: '/authentication/oauth',
   authorizationEndpoint: 'endpointURL',
   redirectUri: window.location.origin + "/authentication/login",
   optionalUrlParams: ['scope'],
   scope: ['user:email'],
   scopeDelimiter: ' ',
   oauthType: '2.0',
   popupOptions: { width: 1020, height: 618 }
   });
```
The name field will be the name you call in the authenticate function.

## Set-Up:

### Steps
1. Create a button in the Interface (`navbar-profile.html`) to initiate the OAuth flow. The button should look like this:
```html
<button class="btn" ng-click="authenticate('name')">Sign In With ####</button>
```  
The ```name``` parameter should be one of the providers listed above, or the name set for a custom provider.

2. in `waltz.properties` set the following properties:
```properties
# Cliet Secret for OAuth
auth.client_secret=
# URL where code is sent to be exchanged for a token
oauth.token_url=
# URL for email resource server
oauth.email_url=
```
3. Ensure that the OAuth provider has the correct redirect URL. This should be the URL of the Waltz instance with `/authentication/login` appended to the end. For example: `http://localhost:8080/authentication/login`



If you have any problems contact:
oscar@agosticconsulting.com