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

package au.org.biodiversity.nslapi

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.swagger.v3.oas.annotations.Hidden

import javax.annotation.security.PermitAll

/*
    This class is for the v1 NSL api endpoint.
    It contains routes for the v1 endpoint
* */
@Slf4j
@Controller("/")
class IndexController {
//    @Inject SearchService searchService
    @Property(name = "nslapi.search.e-limit")
    Integer eLimit

    @Hidden
    @PermitAll
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/prop")
    Map property() {
        log.debug("el: ${eLimit}")
        [status: "OK", exactLimit: "$eLimit"]
    }
}
