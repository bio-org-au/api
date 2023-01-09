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
}
