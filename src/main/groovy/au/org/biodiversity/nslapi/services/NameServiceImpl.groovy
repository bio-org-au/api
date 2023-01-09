package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.jobs.GetReleaseJob
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.time.TimeDuration
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
     * Build and execute graph query for bdr skos output
     *
     * @param String
     * @return HttpResponse
     */
    String getBdrSkosOutput() {
        HttpRequest request = apiAccessService.buildRequest()
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map stats = [
                "bdr_context": response?.body()?.data['dapni_bdr_context'].size(),
                "bdr_sdo": response?.body()['data']['dapni_bdr_graph']['bdr_sdo'][0].size(),
                "bdr_schema": response?.body()['data']['dapni_bdr_graph']['bdr_schema'][0].size(),
                "bdr_labels": response?.body()['data']['dapni_bdr_graph']['bdr_labels'].size(),
                "bdr_top_concept": response?.body()['data']['dapni_bdr_graph']['bdr_top_concept'][0].size(),
                "bdr_concepts": response?.body()['data']['dapni_bdr_graph']['bdr_concepts'][0].size(),
                "bdr_alt_labels": response?.body()['data']['dapni_bdr_graph']['bdr_alt_labels'][0].size(),
                "bdr_unplaced": response?.body()['data']['dapni_bdr_graph']['bdr_unplaced'][0].size()
        ]
        println(stats.toString())
        List finalOutput = []
        finalOutput.add(response?.body()?.data['dapni_bdr_context'][0])
        finalOutput.add( response?.body()['data']['dapni_bdr_graph']['bdr_sdo'][0][0])
        finalOutput.add( response?.body()['data']['dapni_bdr_graph']['bdr_schema'][0][0])
        response?.body()['data']['dapni_bdr_graph']['bdr_labels'][0]?.each {
            finalOutput.add it
        }
        finalOutput.add( response?.body()['data']['dapni_bdr_graph']['bdr_top_concept'][0][0])
        response?.body()['data']['dapni_bdr_graph']['bdr_concepts'][0].each {
            finalOutput.add it
        }
        response?.body()['data']['dapni_bdr_graph']['bdr_alt_labels'][0]?.each {
            finalOutput.add it
        }
        response?.body()['data']['dapni_bdr_graph']['bdr_unplaced'][0].each {
            finalOutput.add it
        }
        // println(finalOutput.toString())
        new File('/tmp','bdr_out.json').withWriter('utf-8') {
         writer -> writer.writeLine(new JsonBuilder(finalOutput).toPrettyString())
        }
        "done"
        // response
    }

    /**
     * Check and process name from a search string;
     * Build graphql request and process the results
     *
     * @param searchText
     * @return HttpResponse
     */
    HttpResponse checkAndProcessName(String searchText, String datasetID) {
        Date start = new Date()
        Date stop = null
        TimeDuration td = null

        // Default variables and pre values
        Map initialResponse = [
            "verbatimSearchString": searchText,
           "datasetSearched": datasetID,
           "license": "http://creativecommons.org/licenses/by/3.0/"
        ]
        Map provMap = ["wasAttributedTo": GetReleaseJob.provenanceUrl]
        if(validateNameString(searchText)) {
            // Build request and get a response
            log.debug("10: ${searchText} ------- start -------")
            HttpRequest request = apiAccessService.buildRequest('post', searchText, datasetID, true)
            HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
            Map responseBodyAsMap = response.body() as Map
            // log.debug("responseBodyAsMap: ${responseBodyAsMap.toString()}...")
            printTime(start, 9)
            // dataset passed or not
            String datasetSearched = (datasetID == '%') ? 'nsl' : datasetID
            initialResponse["datasetSearched"] = datasetSearched
            if (responseBodyAsMap?.get("data")?.get("api_names") == []) {
                // Unmatched names
                log.debug("unMatchString: ${searchText}")
                // Parse with gnparser
                Map atomisedMatchResult = atomisedName(searchText)
                // println("searchText: ${searchText}")
                provMap["appliedProcesses"] = ["gnparser " + atomisedMatchResult?.get("parserVersion")]
                if (atomisedMatchResult?.get("parsed")) {
                    // If parsed
                    String newSearchText = atomisedMatchResult.get("canonical").get("simple")
                    log.debug("newsearchtext: $newSearchText")
                    initialResponse << ["processedSearchString": newSearchText]
                    // make another request and process response
                    HttpRequest newRequest = apiAccessService.buildRequest('post', newSearchText, datasetID, true)
                    HttpResponse<Map> newResponse = httpClient.toBlocking().exchange(newRequest, Map)
                    printTime(start, 6)
                    Map newResponseBodyAsMap = newResponse.body()
                    List finalAllRecords = newResponseBodyAsMap?.get("data")?.get("api_names")
                    // Build no match response with gnparser if empty response
                    if (!finalAllRecords) {
                        printTime(start, 5)
                        HttpResponse.<Map> ok(initialResponse <<
                                ["noOfResults": finalAllRecords.size(),
                                 "resultNameMatchType": "No match",
                                 "provenance": provMap
                                ]
                        )
                    }
                    printTime(start, 4)
                    // Build partial and exact match if response had records
                    HttpResponse.<Map> ok(initialResponse <<
                            ["noOfResults": finalAllRecords.size(),
                             "provenance": provMap,
                             "results": buildMapInRightOrder(finalAllRecords, newSearchText)
                            ]
                    )
                } else {
                    // Build no match response without gnparser
                    printTime(start, 3)
                    HttpResponse.<Map> ok( initialResponse <<
                            ["noOfResults": 0,
                             "processedSearchString": searchText,
                             "resultNameMatchType": "No match",
                             "provenance": provMap
                            ]
                    )
                }

            } else {
                // Add match type to each name
                List allRecords = responseBodyAsMap?.get("data")?.get("api_names")

                // Return all records with the response
                log.debug("matchedString: ${searchText}")
                // Add applied processes to provenance
                provMap["appliedProcesses"] = [ "None" ]
                printTime(start, 2)
                HttpResponse.<Map> ok(initialResponse <<
                        ["noOfResults": allRecords?.size(),
                         "processedSearchString": searchText,
                         "provenance": provMap,
                         "results": buildMapInRightOrder(allRecords, searchText)
                        ]
                )
            }
        } else {
            // When the name validation fails respond with minimal response
            provMap["appliedProcesses"] = [ "None" ]
            printTime(start, 1)
            HttpResponse.<Map> ok(initialResponse <<
                    ["noOfResults": 0,
                     "processedSearchString": "",
                     "provenance": provMap,
                     "reason": "Invalid characters in the name"
                    ]
            )
        }

    }

    /**
     * Perform an atomised search using the GNParser
     * @param String s
     * @return Map
     */
    Map atomisedName(String s) {
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        def command = ["gnparser", s, "-f", "compact"]
        def process = command.execute()
        process.waitFor()
        process.consumeProcessOutput(sout, serr)
        Map atomisedMap = [:]
        if (sout.toString()) {
            atomisedMap = new JsonSlurper().parseText(sout.toString()) as Map
        }
        atomisedMap
    }

    /**
     * Bulk search the map passed as postData
     * @param List of names
     * @return HttpResponse
     */
    @Override
    HttpResponse bulkSearchNames(List names) {
        try {
            // Check names list isnt empty
            if (names) {
                List preparedResponse = []
                names.each { name ->
                    // process each name and append to response
                    preparedResponse.add(checkAndProcessName(name.toString(), '%')?.body())
                }
//                log.debug("bulkSearchNames: ${ preparedResponse.toString()[0..200] }...")
                HttpResponse.ok([
                        "noOfResults": preparedResponse.size(),
                        "results": preparedResponse
                ])
            } else {
                // When list is empty
                HttpResponse.badRequest(["message": "The list of names cannot be empty"])
            }
        } catch(Exception e) {
            // Handle exceptions if any
            HttpResponse.badRequest(["message": e.message])
        }
    }

    /** Validates the data posted to the endpoint
     * to make sure it has the right keys
     *
     * @param Map postData
     * @return Boolean
     */
    @Override
    Boolean validateBulkNamePostData(Map postData) {
        // Check various rules
        // Has key called names and is a list
        postData.containsKey('names') &&
                (postData?.names?.getClass()) == ArrayList &&
                (postData?.names)
    }

    /**
     * Get a list of records and process it to have correct order
     * of fields
     * @param List of Maps records
     * @param search string
     * @return
     */
    @SuppressWarnings('GrMethodMayBeStatic')
    List buildMapInRightOrder(List records, String s) {
        List newRecords = []
        List keysToRemoveAFDAN = ["taxonID", "taxonConceptID", "acceptedNameUsageID", "ccAttributionIRI"]
        List keysToRemoveAFDP = ["parentNameUsageID"]
        records.each { record ->
            Map newRecord = [:]
            // Set the property first
            newRecord['resultNameMatchType'] = (record?.get("scientificName")?.equalsIgnoreCase(s)) ? "Exact" : "Partial"
            // Copy record
            newRecord << record
            // for afd dataset drop some fields
            if (newRecord["datasetName"].toString().equalsIgnoreCase("afd")) {
                List hasUsage = newRecord["hasUsage"]
                hasUsage.each { item ->
                    item["hasAcceptedName"]?.keySet()?.removeAll(keysToRemoveAFDAN)
                    item["hasAcceptedName"]["hasParent"]?.keySet()?.removeAll(keysToRemoveAFDP)
                }
                newRecord.remove("hasUsage")
                newRecord.put("hasUsage", hasUsage)
            }
            // Build it as a list
            newRecords << newRecord
        }
        newRecords
    }

    /**
     * Validate and cleanup a name string
     * @param String s
     * @return HttpResponse
     */
    String validateNameString(String s) {
        (s.contains('%') || !s) ? null : s
    }

    /**
     * Validate and cleanup a dataset string
     * @param String d
     * @return HttpResponse
     */
    @Override
    String validateDatasetString(String d) {
        (!d.matches('^[a-zA-Z]+')) ? null : d
    }

    void printTime(Date start, Integer num) {
        Date stop = new Date()
        log.debug("${num}: ${TimeCategory.minus( stop, start )}")
    }
}

