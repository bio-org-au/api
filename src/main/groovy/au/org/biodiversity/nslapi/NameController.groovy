package au.org.biodiversity.nslapi

import au.org.biodiversity.nslapi.services.ApiAccessService
import au.org.biodiversity.nslapi.services.NameService
import groovy.util.logging.Slf4j
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse

//import au.org.biodiversity.nslapi.services.GraphCallService

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Hidden
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
    @Operation(summary = "Get Skos output for BDR",
            description = "This endpoint delivers SKOS output for BDR data. It delivers data for each shard separately and takes the shard as parameter")
    @ApiResponse(responseCode = "200",
            description = "If valid get request is sent, the endpoint will return a json",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "If invalid http GET request is sent, then the endpoint will return invalid request message as json",
            content = @Content(mediaType = "application/json"))
    @Schema(description = "Get SKOS conformant json-LD data")
    @Tag(name = "label")
    // mn annotations
    @Get("/label")
    @ExecuteOn(TaskExecutors.IO)
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    String label() {
        // Validate name string
        nameService.getBdrSkosOutput()
    }
}