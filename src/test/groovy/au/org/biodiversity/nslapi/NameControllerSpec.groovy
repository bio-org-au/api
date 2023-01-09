package au.org.biodiversity.nslapi

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
class NameControllerSpec extends Specification {
    @Inject
    NameController nameController

    @Shared
    @AutoCleanup
    @Inject
    @Client(value = "/name", configuration = TestHttpClientConfiguration)
    HttpClient httpClient

    def "Check default action returns HttpClientResponseException"() {
        when:
        HttpRequest request = HttpRequest.GET("/")
        httpClient.toBlocking().exchange(request, Map)

        then:
        thrown(HttpClientResponseException)
    }

    def "index method provides error response"() {
        when:
        HttpResponse<Map> response = nameController.index()

        then:
        response.body().message == "Please access one of the endpoints. Example: '/name/check' or '/name/search'"
        response.status == HttpStatus.NOT_FOUND
    }

    def "#uriString causes exception"() {
        when:
        HttpRequest request = HttpRequest.GET(uriString)
        httpClient.toBlocking().exchange(request)

        then:
        thrown(HttpClientResponseException)

        where:
        uriString << ["/check", "/check?q=Acacia%25"] // % - %25
    }

    def "/name/check good"() {
        when:
        HttpRequest request = HttpRequest.GET("/check?q=" + URLEncoder.encode(name, "UTF-8"))
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map body = response.body()

        then:
        response.status() == HttpStatus.OK
        body.noOfResults == rm
        body.verbatimSearchString == ss

        where:
        rm || ss                | name
        0  || "text"            | "text"
        1  || "Acacia"          | "Acacia"
        2  || "Acacia dealbata" | "Acacia dealbata"
    }

    def "check function returns response #searchText"() {
        when:
        HttpResponse response = nameController.check(searchText, 'APNI')

        then:
        response.status() == rs

        where:
        searchText      || rs
        ""              || HttpStatus.BAD_REQUEST
        "Acacia%25"     || HttpStatus.BAD_REQUEST
        "Acacia Mill."  || HttpStatus.OK
    }
}
