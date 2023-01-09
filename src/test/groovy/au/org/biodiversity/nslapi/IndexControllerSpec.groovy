package au.org.biodiversity.nslapi

//import au.org.biodiversity.nslapi.services.SearchService

import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
class IndexControllerSpec extends Specification {
    @Inject
    EmbeddedServer embeddedServer

    @Inject
    IndexController indexController

    @Shared
    @AutoCleanup
    @Inject
    @Client(value = "/", configuration = TestHttpClientConfiguration)
    HttpClient httpClient

//    def "/health function works"() {
//        when:
//        HttpResponse response = indexController.health()
//
//        then:
//        response.status() == HttpStatus.OK
//        response.body() == ["health": "OK", "status": "UP" ]
//    }

    def "/prop function works"() {
        when:
        Map response = indexController.property()

        then:
        response?.status == "OK"
//        response?.exactLimit == "7"
    }
}