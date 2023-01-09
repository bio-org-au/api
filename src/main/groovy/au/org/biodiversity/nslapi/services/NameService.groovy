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
    HttpResponse checkAndProcess(String searchText, String dataset)

    /**
     * Perform an atomised search using the GNParser
     * @param String s
     * @return Map
     */
    Map performAtomisedMatch(String s)
}
