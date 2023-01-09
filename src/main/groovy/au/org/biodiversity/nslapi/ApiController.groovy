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

package au.org.biodiversity.nslapi

import groovy.util.logging.Slf4j
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import jakarta.inject.Inject

import javax.annotation.security.PermitAll
import javax.sql.DataSource

/**
 * User: moziauddin
 * Date: 20/11/2021
 *
 */

@Slf4j
@Controller("/api")
class ApiController {
    @Inject
    ReaderService readerService

    /*
    Return the only record in tree table as map
    @return Map
    @param none
    * */
    @SuppressWarnings("GrMethodMayBeStatic")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/tree")
    Map GetTree() {
        Tuple<Tree> tree = readerService.getTree()
        log.debug "Tree size ${tree.get(0)}"
        if (tree) {
            // Return select fields from tree
            return [
                    "id": tree.id,
                    "accepted": tree.acceptedTree,
                    "config": tree.config,
                    "hp": tree.host_name,
                    "name": tree.name // not set by constructor
            ]
        }
        // Return null is tree object is null
        log.debug "Tree is null"
        return null
    }

}
