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

package au.org.biodiversity.nslapi.jobs


import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * A class to get release version every 30 sec from github repo
 * for nsl api
 */
//@CompileStatic
@Singleton
@Slf4j
class GetReleaseJob {

    String baseUrl = "https://github.com/bio-org-au/nslapi/releases/tag/"
    static String provenanceUrl = ""

    @Inject
    @Client(value = "/")
    private HttpClient httpClient

    @Scheduled(fixedDelay = "24h", initialDelay = "2s")
    void execute() {
        HttpRequest httpRequest = HttpRequest.GET("/info")
        HttpResponse<Map> httpResponse = httpClient.toBlocking().exchange(httpRequest, Map)
        String tag = "v${ httpResponse?.body()?.git?.build?.version }"
        provenanceUrl = (tag ) ? baseUrl + tag : ''
        log.debug(provenanceUrl)
    }
}
