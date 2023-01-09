/*
    Copyright (c) 2021 Australian National Botanic Gardens and Authors

    This file is part of National Species List project.

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

import au.org.biodiversity.nslapi.ApiTaxonView

/**
 * User: moziauddin
 * Date: 20/11/2021
 * This is an interface to implement database reader service
 */

interface ReaderService {
    /**
     * Gets a tuple from the database as ATV Object
     * @param none
     * @return ApiTaxonView object
     */
    ApiTaxonView getRow(String sql)

    /**
     * Gets tuples from the database as a list of ATV Objects
     * @param none
     * @return ApiTaxonView object
     */
    List<ApiTaxonView> getRows(String sql)

    /**
     * Build SQL query with and without wild card to get data from the taxa
     * Three functions to cover optional params
     * @param String searchString,
     *        String column to match,
     *        Boolean rightWildCrd
     * @return String
     */
    public String buildSql(String value)
    public String buildSql(String value, String column)
    public String buildSql(String value, String column, Integer limit)
}