WebFlux SecurityWebFilterChain
==============================

Presentation
------------

Resources protection. In the application.yml, the configuration is setted :

	spring:
	  security:
	    oauth2:
	      client:
	        registration:
	          oidcServices:
	            authorization-grant-type: client_credentials
	            client-id: ${CLIENT_ID}
	            client-secret: ${CLIENT_SECRET}
	            scope:
	            - openid
	        provider:
	          oidcServices:
	            token-uri: ${TOKEN_URI}
	      resourceserver:
	        opaquetoken:
	          client-id: ${CLIENT_ID}
	          client-secret: ${CLIENT_SECRET}
	          introspection-uri: ${INTROSPECTION_URI}
	            
Here, the resourceserver part is very interesting.

@Bean SecurityWebFilterChain
----------------------------

We go protected the **suppliers** resource : **Api.SECURITY_SUPPLIER_PATH**.

I added the **spring-boot-starter-actuator** and i have activated the *info* and *health* features.
You can see, the *info* and *health* features are **permitAll**. In fact, you can call them without problem : no security.
I have precise a cache by the **time-to-live** propertie and it is equals to 10000ms.

But the **suppliers** resource necessary a authenticated and any exchange necessary too an authenticated.
The next builder code precise too an **oauth2ResourceServer** with **opaqueToken** checking.

The code is here :

	@Bean
    public SecurityWebFilterChain securityWebFilterChainFree(ServerHttpSecurity http) {
    	
        http.
        	oauth2Client().
        	and().
        	authorizeExchange().
        	matchers(EndpointRequest.to(Management.INFO, Management.HEALTH)).permitAll().
        	pathMatchers(Api.SECURITY_SUPPLIER_PATH).authenticated().
        	pathMatchers(Api.SECURITY_SUPPLIER_INCLUDED_PATH).authenticated().
        	anyExchange().authenticated().
        	and().
        	oauth2ResourceServer().opaqueToken();
        
        return http.build();
    }

OpaqueToken, What is it ?	
-------------------------

We speak to identity toke, there is too the access token. In fact, the access token is sufficient for the server to identify the client application
and the connected user. Indeed, the token is opaque for the client application, it is recorded in the OAuth2 database (access_token table) with
the client identifier and the expiration date. With this data, we can deduce that the client can or not resources access.

This token is necessary for all applications that it return to OAuth server for client connection checking. By definition, the opaque token is unsigned.
But the OIDC services ( OpenID Connect services) provides a JWT (Json Web Token) that is signed. The access token can be too a JWT.

Request Token
-------------

Here, we speak about **client_credentials** grant type. It exists too the **authorization_code**, we'll see that later.

Example :

	curl -X POST -d "scope=openid" "https://security-domain/token?grant_type=client_credentials"  -H  "accept: application/json"  -H "Authorization: Basic MFIyzzVjNDFkNTEzMWM0ZDI0ZmU1YWQ0MWNhZDI1OWQzMTp2TU5lIU45ngtioooG1vZUVBeDVtZ2syN2pDeWhzQkpCVxx3xx2xxjxwamZxxxx=" -s | jq
	
Parameter : **grant_type=client_credentials**
Authorization header : base64 (user:password)

JWT encoded example :

	eyJraWQiOiJZU3BVal94aFRVRTBSN2dBcmVGUllqMnFNZFRscWdDU1RoanNRYUlrQ3FFIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULmhSQ09LZWZleWk4TnFSOWU1R29ndmRjU3IwLWpVLVN6WC1jWER2NkE4dnciLCJpc3MiOiJodHRwczovL2Rldi00MTE4OTI1Lm9rdGEuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTYxNjU5MTc3NSwiZXhwIjoxNjE2NTk1Mzc1LCJjaWQiOiIwb2FibzJreGJOSzVPQm15UzVkNiIsInVpZCI6IjAwdTd3NmczZ2NYQmRPb3pRNWQ2Iiwic2NwIjpbInJpbzpyZWdpc3RlciJdLCJzdWIiOiJydWR5LnNhbmllekBhZGVvLmNvbSJ9.ej3WDMvOINDkRwfGGtecpnSrXSqDzOXZ1Z8gtabgDXQXs6bQ4DhDaKQZQQI4kJNFOyrgEYQuJ1hhwD_gUL7FePGG7nHSfikytQoQxX6q_W-pnd38PF0EtY0tBCXM1zsfHq3OQpyEUiHZYlihPBYdHqY1IfJH7HLrVCo39ks4puWwMYomu3JRmbLBBRGvjziAkkzqCMQZr7-L4CbTVfOW3zpXCPKZiaHeraHrGz-TsWJuBYu6l325TuKcEtMHbtW2nZDiMJPLdnBXUiovj6yT3NU_3N3JIPQOGwQeVLE11ervgAvi9a7jg4HmslUaQXN_pn7bh3jUrjH1rEVTTo9iCw
	
The JWT is split in 3. The **.** is the separator.

You can use [jwt.io](https://jwt.io) and you paste the token in field name **encoded**. The right side you get the JWT decoded.
The JWT is split in 3 part, The header (the key reference), the payload (json information, expiration, security domain, scope) and the signature.

Header is :

	{
		"kid": "YSpUj_xhTUE0R7gAreFRYj2qMdTlqgCSThjsQaIkCqE",
		"alg": "RS256"
	}

Payload is :

	{
	  "ver": 1,
	  "jti": "AT.hRCOKefeyi8NqR9e5GogvdcSr0-jU-SzX-cXDv6A8vw",
	  "iss": "https://dev-xx189xx.okta.com/oauth2/default",
	  "aud": "api://default",
	  "iat": 1616591775,
	  "exp": 1616595375,
	  "cid": "0oabo2kxbNK5OBmyS5d6",
	  "uid": "00u7w6g3gcXBdOozQ5d6",
	  "scp": [
	    "demo:register"
	  ],
	  "sub": "michael.jordan@bulls.com"
	}

WebClient security
------------------

click [here](https://github.com/rudysaniez/demo-webclient-security)

Link
-----

[An Illustrated Guide to OAuth and OpenID Connect](https://developer.okta.com/blog/2019/10/21/illustrated-guide-to-oauth-and-oidc)

Thanks very much, Okta.

I hope you are now happy! bye.
