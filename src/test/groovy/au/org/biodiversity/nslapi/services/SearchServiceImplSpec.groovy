package au.org.biodiversity.nslapi.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class SearchServiceImplSpec extends Specification{
    @Inject
    ReaderService readerService

    @Inject
    SearchService searchService

    def "bulkSearch - Invalid param throws exceptions"() {
        when:
        searchService.bulkSearch("some string")

        then:
        thrown(MissingMethodException)
    }

    def "bulkSearch - Invalid args returns bad url - arg: #val"() {
        when:
        HttpResponse response = searchService.bulkSearch(val)

        then:
        status == response.status()

        where:
        val                         |   status
        [key: "value"]              |   HttpStatus.BAD_REQUEST
        [names: "value"]            |   HttpStatus.BAD_REQUEST
        [names: 1101]               |   HttpStatus.BAD_REQUEST
        [names: ["a", "b"] ]        |   HttpStatus.OK
    }

    def "bulkSearch - Valid args work - arg: #val"() {
        when:
        HttpResponse response = searchService.bulkSearch(val)

        then:
        status == response.status()
        nr == response.body().namesReceived
        np == response.body().searchResults.matchType

        where:
        val                               |   status            |  nr             | np
        [names: ["Karayevia Round & Bukht."] ]        |   HttpStatus.OK     |  1              | ["Exact"]
        [names: ["Rossithidium"] ]        |   HttpStatus.OK     |  1              | ["Partial"]

    }

    def "getExactMatch test #name with count: #count"() {
        when:
        Map allRecords = searchService.getExactMatch(name)

        then:
        count == allRecords.count
        name << ["Eukaryota", 'Cymbella aspera (Ehrenb.) Cleve', 'Cymbella gastroides (Kütz) Bréb. & Godey', 'Bacillariophyceae classis incertae sedis']

        where:
        name                                        || count
        "Eukaryota"                                 || 3
        'Cymbella aspera (Ehrenb.) Cleve'           || 1
        'Cymbella gastroides (Kütz) Bréb. & Godey'  || 1
        'Bacillariophyceae classis incertae sedis'  || 1
    }

    def "getPartialMatch test #name with count: #count"() {
        when:
        Map allRecords = searchService.getPartialMatches(name)

        then:
        count == allRecords.count
        name << ["Eukaryota", 'Cymbella aspera', 'Cymbella gastroides', 'Bacillariophyceae classis incertae sedis']

        where:
        name                                        || count
        "Eukaryota"                                 || 3
        'Cymbella aspera'                           || 1
        'Cymbella gastroides'                       || 1
        'Bacillariophyceae classis incertae sedis'  || 1
    }

        def "search test #name matched #rm"() {
        when:
        Map allRecords = searchService.search(name)

        then:
        rm == allRecords.recordsMatched
        name << ["Eukaryota", 'Cymbella aspera', 'Cymbella gastroides', 'Bacillariophyceae classis incertae sedis']

        where:
        name                                        || rm
        "Eukaryota"                                 || 3
        'Cymbella aspera'                           || 1
        'Cymbella gastroides'                       || 1
        'Bacillariophyceae classis incertae sedis'  || 1
    }
}
