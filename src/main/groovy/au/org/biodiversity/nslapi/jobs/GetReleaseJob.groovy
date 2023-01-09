package au.org.biodiversity.nslapi.jobs

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Prototype
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton

import static io.micronaut.http.HttpHeaders.ACCEPT
import static io.micronaut.http.HttpHeaders.USER_AGENT

/**
 * A class to get release version every 30 sec from github repo
 * for nsl api
 */
@CompileStatic
@Singleton
@Slf4j
@Prototype
class GetReleaseJob {

    String repoUrl = "https://api.github.com/repos/moziauddin/shared/releases/latest"
    String baseUrl = "https://github.com/moziauddin/shared/releases/tag/"
    String provenanceUrl = ""

    @Inject
    @Client(value = "/")
    private HttpClient httpClient

    @Scheduled(fixedDelay = "1h")
    void execute() {
        HttpRequest httpRequest = HttpRequest.GET(repoUrl)
            .header(USER_AGENT, "MN_H_CLIENT")
            .header(ACCEPT, "application/json")
        HttpResponse<Map> httpResponse = httpClient.toBlocking().exchange(httpRequest, Map)
        provenanceUrl = baseUrl + httpResponse?.body()?.get("tag_name")

        log.debug(provenanceUrl)
    }
}
