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
     * Gets tuples from
     * @param none
     * @return ApiTaxonView object
     */
    ApiTaxonView getRow()

    List<ApiTaxonView> getRows()
}