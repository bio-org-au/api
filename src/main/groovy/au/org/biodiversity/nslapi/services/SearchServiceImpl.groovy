package au.org.biodiversity.nslapi.services

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Slf4j
@CompileStatic
@Singleton
class SearchServiceImpl implements SearchService {
    @Inject
    ReaderService readerService

//    @Property(name = "nslapi.search.partialLimit")
//    Integer partialLimit

    /**
     * Implementation of the search logic to search
     * using both partial and exact match functions
     *
     * @param String searchString
     * @return Map
     */
    Map search (String searchString) {
        // Get exact match results
        Map exactMatchResult = getExactMatch(searchString)
        // Build and return response for exact match
        if(exactMatchResult.count != 0) {
            log.debug "exactMatchResult.results: ${exactMatchResult.results}"
            [ "recordsMatched": exactMatchResult.count,
              "suppliedString": searchString,
              "matchType": "Exact",
              "allRecords": exactMatchResult.results ]
        } else {
            // Get partial matches
            Map partialMatchesResult = getPartialMatches(searchString)
            if (partialMatchesResult.count == 0) {
                // Build and return response for no match
                [ "recordsMatched": partialMatchesResult.count,
                  "suppliedString": searchString,
                  "matchType": "None" ]
            } else {
                // Build and return response for partial match
                [ "recordsMatched": partialMatchesResult.count,
                  "suppliedString": searchString,
                  "matchType": "Partial",
                  "allRecords": partialMatchesResult.results ]
            }
        }
    }

    /**
     * Implementation of the exact match logic
     * @param String searchString
     * @return Map
     */
    @Override
    Map getExactMatch(String searchString) {
        String sql = readerService.buildSql(searchString)
        List allRecords = readerService.getRows(sql)
        [ "count": allRecords?.size(), "results": allRecords ]
    }

    /**
     * Implementation of the partial match logic
     * Gets 50 rows from the DB for partial matched name
     * @param String searchString
     * @return Map
     */
    @Override
    Map getPartialMatches(String searchString) {
        String sql = readerService.buildSql(searchString, "canonicalName", 50)
        List allRecords = readerService.getRows(sql)
        [ "count": allRecords?.size(), "results": allRecords ]
    }

    @Override
    HttpResponse bulkSearch(Map requestBody) {
        // Check if requestBody has a list
        if (requestBody?.names?.getClass() == ArrayList) {
            List response = []
            def namesSent = 0
            def namesProcessed = 0
            try {
                // Get names and search for each one of them
                // Append the response as required
                List namesList = requestBody.names as List
                namesSent = namesList?.size()
                namesProcessed = (namesSent < 10) ? namesSent : 10
                def newnamesList = namesList.take(namesProcessed)
                newnamesList.each { name ->
                    Map nameSearch = search(name as String)
                    response.add(nameSearch)
                }
            } catch(Exception e) {
                // Return an error response on exception
                return HttpResponse.<Map> badRequest(["message": e.getMessage()])
            }
            // If all goes well, respond with results
            return HttpResponse.<Map> ok(["namesReceived": namesSent,
                                          "namesProcessed": namesProcessed,
                                          "searchResults": response])
        } else {
            // Send a error response if names list is not valid or key is missing
            return HttpResponse.<Map> badRequest(
                    ["message": "Missing 'names' key or it does not contain a valid list"])
        }
    }
}