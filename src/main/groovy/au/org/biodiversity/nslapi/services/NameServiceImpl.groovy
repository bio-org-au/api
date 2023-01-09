package au.org.biodiversity.nslapi.services

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Slf4j
@Singleton
class NameServiceImpl implements NameService {
    @Inject
    @Client("/")
    private HttpClient httpClient

    @Inject
    GraphCallService graphCallService

    /**
     * Check and process name from a search string;
     * Build graphql request and process the results
     *
     * @param searchText
     * @return HttpResponse
     */
    HttpResponse checkAndProcess(String searchText, String datasetID) {
        // Build request and get a response
        HttpRequest request = graphCallService.buildRequest('post', searchText, datasetID)
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map responseBodyAsMap = response.body() as Map

        // dataset passed or not
        String datasetSearched = (datasetID == '%') ? 'all' : datasetID
        // Empty response
        if (responseBodyAsMap?.data?.api_names == []) {
            log.debug("unMatchString: ${searchText}")
            HttpResponse.<Map> ok(
                    ["noOfResults": 0,
                     "verbatimSearchString": searchText,
                     "processedSearchString": searchText,
                     "datasetSearched": datasetSearched,
                     "resultNameMatchType": "No match"]
            )
        } else {
            // Add match type to each name
            List allRecords = responseBodyAsMap?.data?.api_names
            // Add matchType to each record found
            allRecords.each { record ->
                record['resultNameMatchType'] = (record?.scientificName?.equalsIgnoreCase(searchText)) ? "Exact" : "Partial"
            }
            // Return all records with the response
            log.debug("matchedString: ${searchText}")
            HttpResponse.<Map> ok(
                    ["noOfResults": allRecords?.size(),
                     "verbatimSearchString": searchText,
                     "processedSearchString": searchText,
                     "datasetSearched": datasetSearched,
                     "provenance": "https://github.com/bio-org-au/nslapi/releases/tag/v0.1.0",
                     "results": allRecords,
                    ]
            )
        }
    }
}
