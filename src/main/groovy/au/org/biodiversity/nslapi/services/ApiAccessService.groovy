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

    /**
     * Create a HttpRequest when a GraphQL query string is supplied
     * as a property
     *
     * @param query
     * @return HttpRequest
     */
    HttpRequest buildRequest( String scheme )
}