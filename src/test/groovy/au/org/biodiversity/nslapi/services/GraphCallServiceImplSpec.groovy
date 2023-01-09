package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.exceptions.InvalidRequestTypeException
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class GraphCallServiceImplSpec extends Specification {
    @Inject
    ApiAccessService apiAccessService

    @Inject
    @Client("/")
    HttpClient httpClient

    // static props can be accessed in individual test methods
    @Property(name = "nslapi.graphql.url")
    String graphQlUri

    static Integer baseQueryLength = 998

    def "buildQuery - Working #searchName - #hasSearchName"() {
        when:
        String query = apiAccessService.buildQuery(searchName, '')

        then:
        hasSearchName == query.contains(searchName)

        where:
        searchName         | hasSearchName
        "Joe Biden"        | true
        "hello there"      | true
        "hello"            | true
        "richard"          | true
    }

    def "buildQuery - #name #length"() {

        when:
        String query = apiAccessService.buildQuery(name, 'APNI')

        then:
        length == query.size()
        query.contains(name)

        where:
        name                            || length
        "blah"                          || baseQueryLength + 4
        "Eucocconeis Cleve"             || baseQueryLength + 17
        "kzjsdbKBdlfngldfgnldfdlfngldfn"|| baseQueryLength + 30
    }

    def "buildRequest - Not Found"() {
        when:
        HttpRequest request = apiAccessService.buildRequest('get', 'acacia', '')
        httpClient.toBlocking().exchange(request, Map)

        then:
        thrown(HttpClientResponseException)
    }

    def "buildRequest - #type #name #rb #rm Found"() {
        when:
        HttpRequest request = apiAccessService.buildRequest(type, name, 'APNI')

        then:
        request.body.toString().contains(rb)
        rm == request.method
        graphQlUri == request.uri.toString()

        where:
        name                        || type   | rb                  | rm
        "blah"                      || 'get'  | 'Optional.empty'    | HttpMethod.GET
        "Acacia"                    || 'post' | 'query'             | HttpMethod.POST
    }

    def "buildRequest - #type #name throws exception"() {
        when:
        HttpRequest request = apiAccessService.buildRequest(type, name, '')

        then:
        thrown(InvalidRequestTypeException)

        where:
        name                      || type
        "Doodia"                  || 'put'
        "Asplenium"               || 'update'
        "Caladenia"               || 'delete'
        "Despicamola"             || 'patch'
        "Dicksonia"               || 'head'
    }
}
