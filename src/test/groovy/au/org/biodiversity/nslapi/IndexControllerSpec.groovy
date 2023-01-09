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