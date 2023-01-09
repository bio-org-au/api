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

import au.org.biodiversity.nslapi.services.ApiAccessService
import au.org.biodiversity.nslapi.services.NameService
import groovy.util.logging.Slf4j
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Hidden

//import au.org.biodiversity.nslapi.services.GraphCallService

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

/*
    This class is for the NSL name api endpoint.
    It contains routes for the name check. search, etc
* */

@Slf4j
@Controller('/name')
class NameController {
    @Inject
    ApiAccessService apiAccessService

    @Inject
    NameService nameService

    @Get("/")
    @SuppressWarnings("GrMethodMayBeStatic")
    @Hidden
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse index() {
        HttpResponse.<Map> notFound([
                "message": "Please access one of the endpoints. Example: '/name/check' or '/name/search'"
        ])
    }

    // /check/name endpoint - GET
    // Swagger Annotations
    @Operation(summary = "Check if a name is partial, exact or no match",
            description = "a name provided as q=nameToMatch will be matched with NSL datasets. This is not a search endpoint. \nExample: 'q=Acacia Mill.'")
    @ApiResponse(responseCode = "200",
            description = "If valid string is provided, the endpoint will return a json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If no string is provided for param 'q', the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404",
            description = "If % is passed for wildcard searches, the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If 'q' value is empty, the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @Schema(description = "Provide a name to match; it will be trimmed and matched case-insensitively", defaultValue = "acacia mill.")
    @Tag(name = "check")
    // mn annotations
    @Get("/check")
    @ExecuteOn(TaskExecutors.IO)
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse check(@Parameter(name = 'q',
            required = true,
            example = "Panax dendroides") @QueryValue('q') String searchText,
                       @Parameter(name = "dataset",
                               required = false,
                               description = "Possible values:  AFD, LNI, AANI, Fungi names, APNI, AusMoss Names; case insensitive",
                               example = "") @Nullable @QueryValue('dataset') String dataSet) {

        // Validate name string
        nameService.validateNameString(searchText)
        // Check if dataset isn't null
        if (!dataSet)
            dataSet = '%'
        nameService.validateDatasetString(dataSet)
        // Cleanup arguments and call service
        nameService.checkAndProcessName(searchText.trim(), dataSet.trim().toLowerCase())
    }

    // /check/name endpoint - POST
    // Swagger Annotations
    @Operation(summary = "Bulk lookup if a name is partial, exact or no match",
            description = "a list of names provided as a json will be matched exactly against NSL datasets. This is not a search endpoint.")
    @ApiResponse(responseCode = "200",
            description = "If valid list of names is provided, the endpoint will return a json response",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If invalid list of names is provided for param 'names', the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404",
            description = "If % char exists in a name in 'names' list, then the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If 'q' value is empty, the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @Schema(description = "Provide a name to match; it will be trimmed and matched case-insensitively", defaultValue = "acacia mill.")
    @RequestBody(
            description = "Provide a urlEncoded list of names to search the NSL dataset",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = Map.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            value = '{"names": ["Neobisnius mediopolitus Lea, 1929", "Acacia leucolobia Sweet", "acacia", "Panax dendroides", "acacia Mill." ]}'
                    )
            )
    )
    @Tag(name = "check")
    // mn annotations
    @ExecuteOn(TaskExecutors.IO)
    @Post("/check")
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse checkBulk(@Body Map body) {
        if (nameService.validateBulkNamePostData(body)) {
//            log.debug("bulk search: ${body}")
            nameService.bulkSearchNames(body.names as List)
        } else {
            HttpResponse.badRequest(["message": 'Invalid body. It needs to be a proper json with a key called names Example: { "names": ["name1", "name2"] }'])
        }
    }

    // /check/name endpoint - GET
    // Swagger Annotations
    @Operation(summary = "Get Skos output links for NSL dataset",
            description = "This endpoint delivers SKOS output as a zip file of six individual datasets namely algae, vascular plants, lichens, mosses, fungi and animals")
    @ApiResponse(responseCode = "200",
            description = "If valid get request is sent, the endpoint will return a json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If invalid http GET request is sent, then the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @Schema(description = "Get SKOS compliant jsonld data as ZIP file including one file for each NSL datasets")
    @RequestBody(
            description = "Provide a body with a key 'fileVersion' in YYYY-MM-DD format",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = Map.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            value = '{"format": "BDR","fileVersion": "2022-08-05"}'
                    )
            )
    )
    @Tag(name = "label")
    // mn annotations
    @Post("/label")
    @ExecuteOn(TaskExecutors.IO)
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse label(@Body Map body) {
        // Send links of the files as a map
        nameService.buildBdrSkosLinks(body)
    }
}