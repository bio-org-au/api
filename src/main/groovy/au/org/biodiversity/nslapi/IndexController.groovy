package au.org.biodiversity.nslapi

import au.org.biodiversity.nslapi.services.SearchService
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
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
import javax.annotation.security.PermitAll

/*
    This class is for the v1 NSL api endpoint.
    It contains routes for the v1 endpoint
* */
@Slf4j
@Controller("/")
class IndexController {
    @Inject
    SearchService searchService
    @Property(name = "nslapi.search.e-limit")
    Integer eLimit

    @Operation(summary = "Searches a name string provided as 'q' query param",
            description = "Passing a search string as 'q=Acacia Mill.' searches it in the NSL dataset and returns a json response")
    @ApiResponse(responseCode = "200", description = "matchType explains whether matched name is exact or partial; No match if name doesnot exist",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "When 'q' uri parameter is not specified")
    @Schema(description = "Provide a name to search", defaultValue = "Acacia dealbata")
    @Tag(name = "check")
    @Get("/check-name")
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse checkName(@Parameter(name = "q", required = true, example = "Acacia dealbata")
                           @QueryValue("q") String searchString) {
        log.debug("search with searchString: '${searchString}'")
        HttpResponse.<Map> ok(searchService.search(searchString))
    }

    @Operation(summary = "Searches a list of name strings provided as a ArrayList in the request body",
            description = "The request body needs to be a valid json with the key 'names' that provides a list. For Example: { \"names\": [ \"Neobisnius mediopolitus Lea, 1929\", \"Acacia leucolobia Sweet\" ] }")
    @ApiResponse(responseCode = "200", description = "When valid json is provided. Response contains the attributes for each of the names in the list provided",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "If a list isn't provided or invalid json is sent")
    @Tag(name = "check")
    @RequestBody(
        description = "Provide a urlEncoded list of names to search the NSL dataset",
        required = true,
        content = @Content(
            schema = @Schema(implementation = Map.class),
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = '{"names": ["Neobisnius mediopolitus Lea, 1929", "Acacia leucolobia Sweet" ]}'
            )
        )
    )
    @SuppressWarnings('GrMethodMayBeStatic')
    @Post("/check-name-bulk")
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse bulkSearch(@Body Map body) {
        searchService.bulkSearch(body)
    }

    /* Health check endpoint
    @param none
    * */
    @Hidden
    @SuppressWarnings('GrMethodMayBeStatic')
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/health")
    HttpResponse health() {
        HttpResponse.ok([ "health": "OK", "status": "UP" ])
    }

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
