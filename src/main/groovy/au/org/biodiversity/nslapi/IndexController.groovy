package au.org.biodiversity.nslapi

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
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



//    @Property(name = "micronaut.config.files")
//    String configFiles

//    @Property(name = "nslapi.db.url")
//    String dbUrl

    /*
    Health check endpoint
    @return Map
    @param none
    * */
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/")
    Map index() {
        // configFiles: ${configFiles}
        log.debug " :: dbUrl: $dbUrl"
        ["health": "ok"]
    }
}
