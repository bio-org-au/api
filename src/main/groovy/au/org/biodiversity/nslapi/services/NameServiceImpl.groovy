package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.jobs.GetReleaseJob
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
    ApiAccessService apiAccessService

    /**
     * Check and process name from a search string;
     * Build graphql request and process the results
     *
     * @param searchText
     * @return HttpResponse
     */
    HttpResponse checkAndProcess(String searchText, String datasetID) {
        // Build request and get a response
        HttpRequest request = apiAccessService.buildRequest('post', searchText, datasetID, true)
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map responseBodyAsMap = response.body() as Map

        // dataset passed or not
        String datasetSearched = (datasetID == '%') ? 'all' : datasetID
        // Empty response
        Map provMap = ["wasAttributedTo": GetReleaseJob.provenanceUrl]
        if (responseBodyAsMap?.get("data")?.get("api_names") == []) {
            log.debug("unMatchString: ${searchText}")
            Map atomisedMatchResult = performAtomisedMatch(searchText)
            if (atomisedMatchResult?.get("parsed")) {
                String newSearchText = atomisedMatchResult.get("canonical").get("simple")
                log.debug("newsearchtext: $newSearchText")
                HttpRequest newRequest = apiAccessService.buildRequest('post', newSearchText, datasetID, true)
                HttpResponse<Map> newResponse = httpClient.toBlocking().exchange(newRequest, Map)
                Map newResponseBodyAsMap = newResponse.body()
                List finalAllRecords = newResponseBodyAsMap?.get("data")?.get("api_names")
                if (!finalAllRecords) {
                    HttpResponse.<Map> ok(
                            ["noOfResults": 0,
                             "verbatimSearchString": searchText,
                             "processedSearchString": newSearchText,
                             "datasetSearched": datasetSearched,
                             "resultNameMatchType": "No match",
                             "GNParserMatched": atomisedMatchResult?.get("parsed"),
                             "GNParserVersion": atomisedMatchResult?.get("parserVersion"),
                             "provenance": provMap
                            ]
                    )
                }
                finalAllRecords.each { record ->
                    record['resultNameMatchType'] = (record?.get("scientificName")?.equalsIgnoreCase(searchText)) ? "Exact" : "Partial"
                }
                HttpResponse.<Map> ok(
                        ["noOfResults": finalAllRecords.size(),
                         "verbatimSearchString": searchText,
                         "processedSearchString": newSearchText,
                         "datasetSearched": datasetSearched,
                         "GNParserMatched": atomisedMatchResult?.get("parsed"),
                         "GNParserVersion": atomisedMatchResult?.get("parserVersion"),
                         "provenance": provMap,
                         "results": finalAllRecords
                        ]
                )
            } else {
                HttpResponse.<Map> ok(
                        ["noOfResults": 0,
                         "verbatimSearchString": searchText,
                         "processedSearchString": searchText,
                         "datasetSearched": datasetSearched,
                         "resultNameMatchType": "No match",
                         "GNParserMatched": atomisedMatchResult?.get("parsed"),
                         "GNParserVersion": atomisedMatchResult?.get("parserVersion"),
                         "provenance": provMap
                        ]
                )
            }

        } else {
            // Add match type to each name
            List allRecords = responseBodyAsMap?.get("data")?.get("api_names")
            // Add matchType to each record found
            allRecords.each { record ->
                record['resultNameMatchType'] = (record?.get("scientificName")?.equalsIgnoreCase(searchText)) ? "Exact" : "Partial"
            }
            // Return all records with the response
            log.debug("matchedString: ${searchText}")

            HttpResponse.<Map> ok(
                    ["noOfResults": allRecords?.size(),
                     "verbatimSearchString": searchText,
                     "processedSearchString": searchText,
                     "datasetSearched": datasetSearched,
                     "provenance": provMap,
                     "results": allRecords
                    ]
            )
        }
    }

    /**
     * Perform an atomised search using the GNParser
     * @param String s
     * @return Map
     */
    Map performAtomisedMatch(String s) {
        HttpRequest gnpRequest = apiAccessService.buildRequest('get', s, '', false)
        HttpResponse<Map> gnpResponse = httpClient.toBlocking().exchange(gnpRequest, Map)
        gnpResponse.body()
    }
}
