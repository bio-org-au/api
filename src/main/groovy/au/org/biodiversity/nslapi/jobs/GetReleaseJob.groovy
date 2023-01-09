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
        String tag = httpResponse?.body()?.git?.tags
        provenanceUrl = (tag ) ? baseUrl + tag : ''
        log.debug(provenanceUrl)
    }
}
