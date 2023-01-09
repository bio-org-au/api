package au.org.biodiversity.nslapi

import au.org.biodiversity.nslapi.services.GraphCallService
import au.org.biodiversity.nslapi.services.NameService
import groovy.util.logging.Slf4j
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

import java.util.regex.Pattern

/*
    This class is for the NSL name api endpoint.
    It contains routes for the name check. search, etc
* */

@Slf4j
@Controller('/name')
class NameController {
    @Inject
    GraphCallService graphCallService

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
    @SuppressWarnings('GrMethodMayBeStatic')
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse check(@Parameter(name = 'q',
            required = true,
            example = "Acacia Mill.") @QueryValue('q') String searchText,
            @Parameter(name = "dataset",
                    required = false,
                    description = "Possible values:  AFD, LNI, AANI, Fungi names, APNI, AusMoss Names; case insensitive",
                    example = "APNI") @Nullable @QueryValue('dataset') String dataSet) {
        if (searchText.contains('%')) {
            HttpResponse.<Map> badRequest(
                    ["status": "error", "message": "You cannot perform a wild-card search on this endpoint"]
            )
        } else if (!searchText) {
            HttpResponse.<Map> badRequest(
                    ["status": "error", "message": "The value for query param 'q' cannot be empty"]
            )
        } else {
            // Check if dataset isn't null
            if (!dataSet)
                dataSet = '%'
            else if (!dataSet.matches('^[a-zA-Z]+'))
                return HttpResponse.<Map> badRequest(
                        ["status": "error", "message": "You cannot use a wildcard in dataset field"]
                )
            // Cleanup arguments and call service
            nameService.checkAndProcess(searchText.trim(), dataSet.trim().toLowerCase())
        }
    }
}