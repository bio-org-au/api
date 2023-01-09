package au.org.biodiversity.nslapi

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import jakarta.inject.Inject

import javax.annotation.security.PermitAll
import javax.sql.DataSource

@Slf4j
@Controller("/")
class IndexController {
    /* Default endpoint; displays html text to documentation
    * */
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    @Get("/")
    HttpResponse index() {
        HttpResponse.ok(
                """
                        NSL API Version 1.0. 
                        Refer to our documentation at <a href="/docs">here</a>
                        """
        )
    }

    /* Health check endpoint
    @param none
    * */
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/health")
    Map health() {
        [
            "health": "OK",
            "status": "UP"
        ]
    }
}
