package au.org.biodiversity.nslapi.services

import io.micronaut.http.HttpRequest

/**
 * Interface to use to connect to an API or graph service
 */
interface ApiAccessService {

    /**
     * Create a GraphQL or webservice queries to use in a request
     *
     * @param dataset
     * @param name
     * @return HttpRequest
     */
    String buildGraphQuery(String name, String dataset)

    /**
     * Create a HttpRequest object to use in a call
     *
     * @param String name
     * @param String dataset
     * @return HttpRequest
     */
    HttpRequest buildRequest(String type, String name, String datasetID, Boolean graphRequest)

    /**
     * Generate a qraph query using variables to it can be
     * re-purposed in the future.
     *
     * @param String type
     * @param String name
     * @param String datasetID
     * @param Boolean graphRequest
     * @return String
     */
    String generateGraphQuery(String name, String dataset)
}