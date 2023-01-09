//package au.org.biodiversity.nslapi.services
//
//import au.org.biodiversity.nslapi.exceptions.InvalidRequestTypeException
//import groovy.transform.CompileStatic
//import groovy.util.logging.Slf4j
//import io.micronaut.context.annotation.Property
//import io.micronaut.http.HttpRequest
//import jakarta.inject.Singleton
//
///*
//    Implementation of the ReaderService
//* */
//@Singleton
//@CompileStatic
//@Slf4j
//@SuppressWarnings('GrMethodMayBeStatic')
//class GraphCallServiceImpl implements GraphCallService {
//    @Property(name = "nslapi.graphql.url")
//    String graphEndpoint
//
//    @Property(name = "nslapi.graphql.adminSecret")
//    String graphqlAdminSecret
//
//    /**
//     * Create a GraphQL query to use in the request
//     *
//     * @param dataset
//     * @param name
//     * @return HttpRequest
//     */
//    String buildQuery(String name, String dataset) {
//        String checkSciOrCan = '{ "query" : "query checkName($name_to_match: String!, $dataset_name: String!) { api_names(where: {_and: {_or: [{scientificName: {_ilike: $name_to_match}}, {canonicalName: {_ilike: $name_to_match}}], datasetName: {_ilike: $dataset_name}}}) { scientificName resultNameMatchStatus:taxonomicStatus nameType taxonRank scientificNameID canonicalName scientificNameAuthorship nomIlleg nomInval nomenclaturalCode nomenclaturalStatus nameSource:datasetName taxonRankSortOrder ccAttributionIRI hasUsage { taxonID nameType taxonomicStatus proParte taxonRank taxonConceptID nameAccordingToID ccAttributionIRI hasAcceptedNameUsage { taxonID scientificName taxonRank scientificNameID canonicalName scientificNameAuthorship taxonConceptID parentNameUsageID nameType taxonomicStatus proParte kingdom family taxonSource:datasetName nameAccordingTo nameAccordingToID taxonRemarks taxonDistribution higherClassification license ccAttributionIRI } } } }", "variables": { "name_to_match": "' + name + '", "dataset_name": "' + dataset + '" } }'
////        log.debug("checkSciOrCan: $checkSciOrCan")
//        checkSciOrCan
//    }
//
//    /**
//     * Create a GraphQL hasura query and build a request object
//     *
//     * @param requestType
//     * @param name
//     * @return HttpRequest
//     */
//    HttpRequest buildRequest(String requestType, String searchString, String datasetID) {
//        HttpRequest request = null
//        // Build correct request type
//        switch (requestType.toLowerCase()) {
//            case 'get':
//                request = HttpRequest.GET(graphEndpoint)
//                break
//            case 'post':
//                request = HttpRequest.POST(graphEndpoint, buildQuery(searchString, datasetID))
//                        .header('x-hasura-admin-secret', graphqlAdminSecret)
//                        .header('Content-Type', 'application/json')
//                break
//            default:
//                log.debug("Unable to build a request for invalid type: $requestType")
//                throw new InvalidRequestTypeException("Unable to build a request for invalid type: $requestType")
//        }
//        request
//    }
//}
