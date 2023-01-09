package au.org.biodiversity.nslapi

import au.org.biodiversity.nslapi.services.SearchService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest
class IndexControllerSpec extends Specification {
    @Inject
    SearchService searchService

    @Inject
    EmbeddedServer embeddedServer

    @Inject
    IndexController indexController

    @Shared
    @AutoCleanup
    @Inject
    @Client(value = "/", configuration = TestHttpClientConfiguration)
    HttpClient httpClient

    def "/health function works"() {
        when:
        HttpResponse response = indexController.health()

        then:
        response.status() == HttpStatus.OK
        response.body() == ["health": "OK", "status": "UP" ]
    }

    def "/check-name-bulk function works"() {
        when:
        HttpResponse response = indexController.bulkSearch(["names": ["Acacia", "Acacia dealbata"]])

        then:
        response.status == HttpStatus.OK
    }

    def "/check-name function works"() {
        when:
        HttpResponse response = indexController.checkName("Acacia")

        then:
        response.status == HttpStatus.OK
    }


    def "/prop function works"() {
        when:
        Map response = indexController.property()

        then:
        response.status == "OK"
    }

    def "test root uri is accessible"() {
        when: "accessing the root uri"
        HttpRequest request = HttpRequest.GET("/")
        HttpResponse response = httpClient.toBlocking().exchange(request)

        then: "Response is OK"
        response.status() == HttpStatus.OK
    }

    def "check-name 0 params throws exception"() {
        when:
        HttpRequest cnBadRequest = HttpRequest.GET("/check-name")
        httpClient.toBlocking().exchange(cnBadRequest)

        then:
        thrown(HttpClientResponseException)
    }

    def "check-name invalid params throws exception"() {
        when:
        HttpRequest cnBadRequest = HttpRequest.GET("/check-name?blah=foo")
        httpClient.toBlocking().exchange(cnBadRequest)

        then:
        thrown(HttpClientResponseException)
    }

    @Unroll
    def "check-name-OK #mt #rm records - #queryString"() {
        when:
        HttpRequest cnGoodrequest = HttpRequest.GET("/check-name?q=${URLEncoder.encode(queryString, "UTF-8")}")
        HttpResponse<Map> cnGoodresponse = httpClient.toBlocking().exchange(cnGoodrequest, Map)

        then:
        status == 200
        rm == cnGoodresponse.body().recordsMatched
        ss == cnGoodresponse.body().suppliedString
        mt == cnGoodresponse.body().matchType
        queryString << ["xyz",
                        "Bacillariaceae",
                        "Cylindrotheca Rabenh.",
                        "Tripos dens var. reflexus",
                        "Cylindrotheca closterium",
                        "Planothidium Round & Bukht.",
                        "Cymbella gastroides"]

        where:
        queryString                   || status | rm | mt        | ss
        "xyz"                         || 200    | 0  | "None"    | "xyz"
        "Bacillariaceae"              || 200    | 1  | "Exact"   | "Bacillariaceae"
        "Cylindrotheca Rabenh."       || 200    | 1  | "Exact"   | "Cylindrotheca Rabenh."
        "Tripos dens var. reflexus"   || 200    | 1  | "Partial" | "Tripos dens var. reflexus"
        "Cylindrotheca closterium"    || 200    | 1  | "Partial" | "Cylindrotheca closterium"
        "Planothidium Round & Bukht." || 200    | 1  | "Exact"   | "Planothidium Round & Bukht."
        "Cymbella gastroides"         || 200    | 1  | "Partial" | "Cymbella gastroides"
    }

    def "check-name-bulk - GET throws exception"() {
        when:
        HttpRequest request = HttpRequest.GET("/check-name-bulk")
        httpClient.toBlocking().exchange(request, Map)

        then:
        thrown(HttpClientResponseException)
    }

    def "check-name-bulk - POST no body throws exception"() {
        when:
        HttpRequest request = HttpRequest.POST("/check-name-bulk", "")
        httpClient.toBlocking().exchange(request)

        then:
        thrown(HttpClientResponseException)
    }

    def "check-name-bulk - POST valid #nr #np #rm #bodyMap"() {
        when:
        HttpRequest request = HttpRequest.POST("/check-name-bulk", bodyMap)
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)

        then:
        bodyMap << [
                '{"names": ["blah"]}'
        ]
        response.status() == HttpStatus.OK
        nr == response.body().namesReceived
        np == response.body().namesProcessed
        rm == response.body().searchResults['recordsMatched'][0]

        where:
        bodyMap                                 || status   | nr    | np        | rm
        '{"names": ["blah"]}'                   || 200      | 1     | 1         | 0
        '{"names": ["Prymnesiophycidae"]}'      || 200      | 1     | 1         | 1
        '{"names": ["Bacillaria paradoxa J.F.Gmel.", "Nitzschia closterium (Ehrenb.) W.Sm.", "Tripos dens var. reflexus (E.J.Schmidt) F.Gómez"]}'      || 200      | 3     | 3         | 1
    }
}