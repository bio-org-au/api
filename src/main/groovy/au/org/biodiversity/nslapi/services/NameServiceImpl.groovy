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

package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.jobs.GetReleaseJob
import au.org.biodiversity.nslapi.util.Performance
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.text.SimpleDateFormat

@Slf4j
@Singleton
class NameServiceImpl implements NameService {
    @Inject
    @Client("/")
    private HttpClient httpClient

    @Inject
    ApiAccessService apiAccessService

    @Property(name = 'nslapi.app.environ')
    String envPropertyName

    @Property(name = 'nslapi.gnparser.endpoint')
    String gnparserEndpointProperty

    /**
     * Build bdr skos output links and return a map
     *
     * @param Map b
     * @return HttpResponse
     */
    HttpResponse buildBdrSkosLinks(Map b) {
        String fileEndpoint = 'https://api.biodiversity.org.au/name/label/file.html'
        if (b) {
            if (b.getClass() == LinkedHashMap &&
                    b.containsKey('format')) {
                HttpResponse response = httpClient.toBlocking().exchange(HttpRequest.GET(fileEndpoint), String)
                String filenames = response.body().trim()
                        .replace("\n", ' ')
                        .replace('BDR_name_label_', '')
                        .replace('.zip', '')
                if (b.get('format').toString().toLowerCase() == 'bdr') {
                    String fileVersionValue = b.get('fileVersion').toString()?.replace('-', '')
                    if (filenames.contains(fileVersionValue)) {
                        String genFileName = "BDR_name_label_${fileVersionValue}.zip"
                        log.info("Filename generated: ${genFileName}")
                        HttpResponse.ok([
                                status: HttpStatus.OK,
                                "link": "https://api.biodiversity.org.au/name/label/" + genFileName
                        ])
                    } else {
                        List filenamesList = filenames.split(' ')
                        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd")
                        Date now = new Date()
                        String todaysFileName = 'BDR_name_label_' + sdf.format(now).toString() + ".zip"
                        String link = "https://api.biodiversity.org.au/name/label/${todaysFileName}"
                        String message = "${b.get('fileVersion').toString()} did not match any fileVersion, " +
                                "returning a link to the most current fileVersion. A link to all available versions " +
                                "can be found in allVersionsLink attribute in this response"
                        String allVersionsLink = "https://api.biodiversity.org.au/name/label/index.html"
                        HttpResponse.ok([
                                "status": HttpStatus.OK,
                                "link": link,
                                "message": message,
                                "allVersionsLink": allVersionsLink
                        ])
                    }
                } else {
                    HttpResponse.badRequest([
                            "status": "error",
                            message: "The format field in the body has invalid value, " +
                                    "pass a valid value. Example: BDR"
                    ])
                }
            } else {
                HttpResponse.badRequest([
                        "status": "error",
                        "message": "The body is not a valid object or does not " +
                                "contain a field called 'format'"
                ])
            }

        } else {
            HttpResponse.badRequest([
                    "status": "error",
                    "message": "The request is missing body"
            ])
        }
    }

    /**
     * Check and process name from a search string;
     * Build graphql request and process the results
     *
     * @param searchText String
     * @param datasetID String
     * @return HttpResponse
     */
    HttpResponse checkAndProcessName(String searchText, String datasetID) {
        Date start = new Date()

        // Default variables and pre values
        Map initialResponse = [
                "verbatimSearchString": searchText,
                "datasetSearched": datasetID,
                "license": "https://creativecommons.org/licenses/by/3.0/"
        ]
        Map provMap = ["wasAttributedTo": GetReleaseJob.provenanceUrl]
        if(validateNameString(searchText)) {
            // Build request and get a response
            // log.debug("10: ${searchText} ------- start -------")
            HttpRequest request = apiAccessService.buildRequest('post', searchText, datasetID, true)
            HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
            Map responseBodyAsMap = response.body() as Map
            // log.debug("responseBodyAsMap: ${responseBodyAsMap.toString()}...")
            // Performance.printTime(start, 9)
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
                    // Performance.printTime(start, 6)
                    Map newResponseBodyAsMap = newResponse.body()
                    List finalAllRecords = newResponseBodyAsMap?.get("data")?.get("api_names")
                    // Build no match response with gnparser if empty response
                    if (!finalAllRecords) {
                        log.warn " { \"path\": 5, \"requestOutput\": { \"duration\": \"${Performance.getTime(start)}\", \"nameProvided\": \"${searchText}\", \"nameSearched\": \"${newSearchText}\", \"matchType\": \"No Match\", \"gnparser\": \"yes\" } }"
                        HttpResponse.<Map> ok(initialResponse <<
                                ["noOfResults": finalAllRecords.size(),
                                 "resultNameMatchType": "No match",
                                 "provenance": provMap
                                ]
                        )
                    } else {
                        log.warn " { \"path\": 4, \"requestOutput\": { \"duration\": \"${Performance.getTime(start)}\", \"nameProvided\": \"${searchText}\", \"nameSearched\": \"${newSearchText}\", \"matchType\": \"Matched\", \"gnparser\": \"yes\" } }"
                        // Build partial and exact match if response had records
                        HttpResponse.<Map> ok(initialResponse <<
                                ["noOfResults": finalAllRecords.size(),
                                 "provenance": provMap,
                                 "results": buildMapInRightOrder(finalAllRecords, newSearchText)
                                ]
                        )
                    }
                } else {
                    // Build no match response without gnparser
                    log.warn " { \"path\": 3, \"requestOutput\": { \"duration\": \"${Performance.getTime(start)}\", \"nameSearched\": \"${searchText}\", \"matchType\": \"No match\", \"gnparser\": \"no\" } }"
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
                log.warn " { \"path\": 2, \"requestOutput\": { \"duration\": \"${Performance.getTime(start)}\", \"nameSearched\": \"${searchText}\", \"matchType\": \"Matched\", \"gnparser\": \"no\" } }"
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
            log.warn " { \"path\": 1, \"requestOutput\": { \"duration\": \"${Performance.getTime(start)}\", \"nameSearched\": \"\", \"matchType\": \"No Match\", \"gnparser\": \"no\" } }"
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
     * Search a name based on a search string
     * @param String s
     * @return HttpResponse
     */
    HttpResponse searchNameByString(String searchText) {
        String query = '{"query" : "query NameSearch1($name: String) { api_testapni_name_cv_aggregate(where: {fullName: {_iregex: $name}}) { aggregate { count } } api_testapni_name_cv(where: {isScientific: {_eq: true}, isCultivar: {_eq: false}, isHybrid: {_eq: false}, fullName: {_iregex: $name}}, order_by: {sortName: asc}, limit: 100, offset: 0) { family fullName simpleName genericName specificEpithet authorship nameId nameType rank datasetName nomenclaturalCode nomenclaturalStatus primaryUsageType identifier license isAutonym isCultivar isHybrid isNameFormula isNomIlleg isNomInval isScientific kingdom modified referenceCitation sourceId uninomial typeCitation } }", "variables": { "name": "' + searchText + '" } }'
        log.warn(query)
        Date start = new Date()

        // Default variables and pre values
        Map initialResponse = [
                "verbatimSearchString": searchText,
                "license"             : "https://creativecommons.org/licenses/by/3.0/"
        ]
        HttpRequest request = apiAccessService.buildRequestWithQuery('post', query, true)
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map responseBodyAsMap = response.body() as Map
        log.debug(responseBodyAsMap.toString())

        if (response.body().containsKey('errors')) {
            log.error("ERROR MESSAGE:  ${response.body().errors.extensions.internal.error}")
            HttpResponse.serverError(response.body().errors.extensions.internal.error.toString())
        } else {
            if (responseBodyAsMap?.get("data")?.get("api_names") == []) {

            } else {
                Map provMap = ["wasAttributedTo": GetReleaseJob.provenanceUrl]
                Long noOfResults = responseBodyAsMap['data']['api_testapni_name_cv_aggregate']['aggregate']['count'] as Long
                List results = responseBodyAsMap['data']['api_testapni_name_cv']
                HttpResponse.<Map> ok(initialResponse <<
                        ["noOfResults": noOfResults,
                         "processedSearchString": searchText,
                         "provenance": provMap,
                         "results": results
                        ]
                )
            }
        }
    }

    /**
     * Perform an atomised search using the GNParser
     * @param String s
     * @return Map
     */
    Map atomisedName(String s) {
        String urlEncodedName = URLEncoder.encode(s, "utf-8")
        String url = gnparserEndpointProperty + urlEncodedName
        HttpRequest request = apiAccessService.buildRequest(gnparserEndpointProperty, 'get', s, '', false)
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)
        Map atomisedMap = response.body() as Map
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

    Boolean validateVersion(String v) {
        if (v && v.length() == 8) {
            List date = [v[0..3], v[4..5], v[5..6]]
        } else { false }
    }
}