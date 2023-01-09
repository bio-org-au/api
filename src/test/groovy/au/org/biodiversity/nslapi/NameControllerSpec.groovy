/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL API project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

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

//    def "#uriString causes exception"() {
//        when:
//        HttpRequest request = HttpRequest.GET(uriString)
//        httpClient.toBlocking().exchange(request)
//
//        then:
//        thrown(HttpClientResponseException)
//
//        where:
//        uriString << ["/check", "/check?q=Acacia%25"] // % - %25
//    }

//    def "/name/check good"() {
//        when:
//        HttpRequest request = HttpRequest.GET("/check?q=" + URLEncoder.encode(name, "UTF-8"))
//        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
//        Map body = response.body()
//
//        then:
//        response.status() == HttpStatus.OK
//        body.noOfResults == rm
//        body.verbatimSearchString == ss
//
//        where:
//        rm || ss                | name
//        0  || "text"            | "text"
//        1  || "Acacia"          | "Acacia"
//        2  || "Acacia dealbata" | "Acacia dealbata"
//    }

    def "check function returns response #searchText"() {
        when:
        HttpResponse response = nameController.check(searchText, 'APNI')

        then:
        response.status() == rs

        where:
        searchText      || rs
        ""              || HttpStatus.OK
        "Acacia%25"     || HttpStatus.OK
        "Acacia Mill."  || HttpStatus.OK
    }

//    def "validateBulkNamePostData - check #names is #status"() {
//        when:
//        HttpResponse response = nameController.checkBulk(names)
//
//        then:
//        response.status() == status
//
//        where:
//        status  | names
//        HttpStatus.BAD_REQUEST    | ["name": ""]
//        HttpStatus.BAD_REQUEST    | ["names": ""]
//        HttpStatus.BAD_REQUEST    | ["names": []]
//        HttpStatus.BAD_REQUEST    | ["names": ["somename"]]
//        HttpStatus.OK    | ["names": ["somename", "acacia", "doodia"]]
//    }
}
