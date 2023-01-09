package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.exceptions.InvalidRequestTypeException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import jakarta.inject.Singleton

/*
    Implementation of the ApiAccessService
* */
@Singleton
@CompileStatic
@Slf4j
@SuppressWarnings('GrMethodMayBeStatic')
class ApiAccessServiceImpl implements ApiAccessService {
    @Property(name = "nslapi.graphql.url")
    String graphEndpoint

    @Property(name = "nslapi.graphql.adminSecret")
    String graphqlAdminSecret

    @Property(name = "nslapi.gnparser.apiUrl")
    String gnparserApiEndpoint

    @Property(name = "nslapi.queries.bdr-skos")
    String bdrSkosQuery

    /**
     * Create a GraphQL or web service API query to use in the request
     * TODO: Build a class to generate queries on the fly and keep history
     *
     * @param dataset
     * @param name
     * @return HttpRequest
     */
    String buildGraphQuery(String name, String dataset) {
        String checkSciOrCan = '{ "query" : "query checkName($name_to_match: String!, $dataset_name: String!) { api_names(where: {_and: {_or: [{scientificName: {_ilike: $name_to_match}}, {canonicalName: {_ilike: $name_to_match}}], datasetName: {_ilike: $dataset_name}}}) { scientificName resultNameMatchStatus:taxonomicStatus nameType taxonRank scientificNameID canonicalName scientificNameAuthorship nomIlleg nomInval nomenclaturalCode nomenclaturalStatus nameSource:datasetName taxonRankSortOrder ccAttributionIRI hasUsage { taxonID nameType taxonomicStatus proParte taxonRank taxonConceptID nameAccordingToID ccAttributionIRI hasAcceptedNameUsage { taxonID scientificName taxonRank scientificNameID canonicalName scientificNameAuthorship taxonConceptID parentNameUsageID nameType taxonomicStatus proParte kingdom family taxonSource:datasetName nameAccordingTo nameAccordingToID taxonRemarks taxonDistribution higherClassification license ccAttributionIRI } } } }", "variables": { "name_to_match": "' + name + '", "dataset_name": "' + dataset + '" } }'
//        log.debug("buildGraphQuery: $checkSciOrCan")
        checkSciOrCan
    }

    /**
     * Generate a GraphQL query and return it
     *
     * @param requestType
     * @param name
     * @return HttpRequest
     */
    String generateGraphQuery(String name, String dataset){
        String queryDeclaration = '"query checkName($name_to_match: String!, $data_set: String) { '
        String queryArguments = 'api_names(where: {_and: {_or: [{scientificName: {_ilike: $name_to_match}}, {canonicalName: {_ilike: $name_to_match}}], datasetName: {_ilike: $data_set}}}) { '
        String queryProperties = 'scientificName resultNameMatchStatus:taxonomicStatus nameType taxonRank scientificNameID ' +
                'scientificName scientificNameID taxonomicStatus nameType taxonRank canonicalName scientificNameAuthorship nomIlleg ' +
                'nomInval nomenclaturalCode ccAttributionIRI datasetName nomenclaturalStatus hasUsage { taxonomicStatus proParte ' +
                'hasAcceptedName { taxonRank scientificName scientificNameID nomenclaturalStatus nameType ' +
                'canonicalName scientificNameAuthorship kingdom family nameAccordingTo taxonID ' +
                'taxonConceptID higherClassification taxonDistribution datasetName ccAttributionIRI ' +
                'hasParent { parentScientificName:scientificName parentScientificNameID:scientificNameID ' +
                'parentNameUsageID:taxonID } } } } }"'
        String queryVariables = ', "variables": { "name_to_match": "' + name + '", "data_set": "' + dataset + '" } '
        String generatedQuery = '{"query" : ' +
                "${queryDeclaration} " +
                "$queryArguments" +
                "$queryProperties" +
                "$queryVariables" +
                '}'
//        log.debug("Gen Query: " + generatedQuery)
        generatedQuery
    }

    /**
     * Create a GraphQL query and build a request object
     *
     * @param requestType
     * @param name
     * @return HttpRequest
     */
    HttpRequest buildRequest(String requestType, String searchString, String datasetID, Boolean graphRequest = true) {
        HttpRequest request = null
        String endpoint = graphRequest ? graphEndpoint : gnparserApiEndpoint
        // Build correct request type

        switch (requestType.toLowerCase()) {
            // Create a get request
            case 'get':
                request = HttpRequest.GET(endpoint + URLEncoder.encode(searchString, "UTF-8"))
                break
            // Create a post request
            case 'post':
                if (graphRequest) {
                    // For graphql type post request
                    request = HttpRequest.POST(endpoint, generateGraphQuery(searchString, datasetID))
                            .header('x-hasura-admin-secret', graphqlAdminSecret)
                            .header('Content-Type', 'application/json')
                } else {
                    // Placeholder post request for future use/enhancement
                    String postData = ""
                    request = HttpRequest.POST(endpoint, postData)
                }
                break
            // For all other type of requests
            default:
                log.debug("Unable to build a request for invalid type: $requestType")
                throw new InvalidRequestTypeException("Unable to build a request for invalid type: $requestType")
        }
//        log.debug("Request: $request")
        request
    }

    /**
     * Create a HttpRequest when a GraphQL query string is supplied
     * as a property
     *
     * @param query
     * @return HttpRequest
     */
    HttpRequest buildRequest() {
        HttpRequest request = null
        // Currently only used bye bdr skos query
        // TODO: make is general purpose
        String graphQuery = bdrSkosQuery
        if (graphQuery) {
            request =  HttpRequest.POST(graphEndpoint, graphQuery)
                    .header('x-hasura-admin-secret', graphqlAdminSecret)
                    .header('Content-Type', 'application/json')
        }
        request
    }
}