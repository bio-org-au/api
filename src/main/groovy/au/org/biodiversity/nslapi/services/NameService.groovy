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
    HttpResponse buildBdrSkosLinks(Map b)
}
