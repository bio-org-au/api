package au.org.biodiversity.nslapi.services

import io.micronaut.http.HttpResponse

/**
 * Service to work with names
 */
interface NameService {
    /**
     * Check and process name from a search string
     * @param searchText
     * @return HttpResponse
     */
    HttpResponse checkAndProcessName(String searchText, String dataset)

    /**
     * Perform an atomised search using the GNParser
     * @param String s
     * @return Map
     */
    Map atomisedName(String s)

    Boolean validateBulkNamePostData(Map postData)

    HttpResponse bulkSearchNames(List names)

    String validateNameString(String s)

    String validateDatasetString(String d)

    /**
     * Build and execute graph query for bdr skos output
     *
     * @param String
     * @return HttpResponse
     */
    String getBdrSkosOutput()
}
