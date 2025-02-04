---
layout: post
title: "MicroProfile JWT"
---

MicroShed Testing provides integration with [MicroProfile JWT](https://github.com/eclipse/microprofile-jwt-auth) applications. MicroProfile JWT
is a specification that standardizes OpenID Connect (OIDC) based JSON Web Tokens (JWT) usage in Java applications.

## Sample MP JWT secured endpoint

Typically MP JWT is used to secure REST endpoints using the `@javax.annotation.security.RolesAllowed` annotation at either the class or method level. Suppose we have a REST endpoint secured with MP JWT as follows:

```java
@Path("/data")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecuredService {
   
    @PermitAll
    @GET
    @Path("/ping")
    public String ping() {
        return "ping";
    }
    
    @GET
    @RolesAllowed("users")
    @Path("/users")
    public String getSecuredInfo() {
        return "this is some secured info";
    }
}
```

As the `@RolesAllowed` annotations imply, anyone can access the `GET /data/ping` endpoint, but only client authenticated in the `users` role can access the `GET /data/users` endpoint.

## Testing a MP JWT secured endpoint

When MicroShed Testing will automatically generate and configure a pair of JWT secrets for the `MicroProfileApplication` container. Then a test client may access these endpoints using the `@JwtConfig` annotation on injected REST clients as follows:

```java
@MicroShedTest
public class SecuredSvcTest {

    @Container
    public static MicroProfileApplication<?> app = new MicroProfileApplication<>()
                    .withAppContextRoot("/")
                    .withReadinessPath("/data/ping");

    @Inject
    @JwtConfig(claims = { "groups=users" })
    public static SecuredService securedSvc;

    @Inject
    @JwtConfig(claims = { "groups=wrong" })
    public static SecuredService misSecuredSvc;

    @Inject
    public static SecuredService noJwtSecuredSvc;

    @Test
    public void testGetSecuredInfo() {
        String result = securedSvc.getSecuredInfo();
        assertTrue(result.contains("this is some secured info"));
    }

    @Test
    public void testGetSecuredInfoBadJwt() {
        // user will be authenticated but not in role, expect 403
        assertThrows(ForbiddenException.class, () -> misSecuredSvc.getSecuredInfo());
    }

    @Test
    public void testGetSecuredInfoNoJwt() {
        // no user, expect 401
        assertThrows(NotAuthorizedException.class, () -> noJwtSecuredSvc.getSecuredInfo());
    }
}
```

In the above code example, the `securedSvc` REST client will be generated with the correct JWT key that has been configured on the `app` container, along with the group claim `users`. The result is that the `secureSvc` REST client can successfully access the `GET /data/users` endpoint, which is restricted to clients in the `users` role.

The `noJwtSecuredSvc` REST client will be generated with no JWT header, and the `misSecuredSvc` client will be generated with an invalid group claim. As a result, neither of these REST clients will be able to sucessfully access the `GET /data/users` secured endpoint, as expected.

## Learning resources

- [Tomitribe blog explaining MicroProfile JWT](https://www.tomitribe.com/blog/microprofile-json-web-token-jwt/)
- [OpenLiberty guide on using MicroProfile JWT](https://openliberty.io/guides/microprofile-jwt.html)
