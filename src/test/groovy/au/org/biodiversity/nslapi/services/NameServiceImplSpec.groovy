package au.org.biodiversity.nslapi.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class NameServiceImplSpec extends Specification {
    @Inject
    NameServiceImpl nameService


//    def "name/check - #name matches #rm records"() {
//        when:
//        HttpResponse response = nameService.checkAndProcessName(name, '%')
//
//        then:
//        response.status() == HttpStatus.OK
//        response.body()?.noOfResults == rm
//        response.body()?.license == 'http://creativecommons.org/licenses/by/3.0/'
//        response.body()?.datasetSearched == 'nsl'
//        response.body()?.results?.resultNameMatchType == rmt
//
//
//        where:
//        rm  |  rmt                              | name                                 | processedSearchString
//        0   | null                              | "hello"                              | "hello"
//        2   | ["Partial", "Partial"]            | "Acacia dealbata"                    | "Acacia dealbata"
//        0   | []                     | "Acacia dealblahta"                  | "Acacia"
//        3   | ["Partial", "Partial", "Partial"] | "Panax dendroides"                   | "Panax dendroides"
//        1   | ["Exact"]                         | "Neobisnius mediopolitus Lea, 1929"  | "Neobisnius mediopolitus Lea, 1929"
//        1   | ["Exact"]                          | "Acacia leucolobia Sweet"            | "Acacia leucolobia Sweet"
//    }

//    def "name/check - POST #names matches #rm records"() {
//        when:
//        HttpResponse response = nameService.bulkSearchNames(names)
//
//        then:
//        response.status() == status
//        response.body()?.noOfResults == rm
//        response.body()?.results[0]?.noOfResults == rm1
//
//        where:
//        status                  | rm1     | rm     | names
//        HttpStatus.OK           | 0       | 2      | ["hello", "hi"]
//        HttpStatus.OK           | 2       | 2      | ["Acacia dealbata", "Doodia"]
//        HttpStatus.OK           | 1       | 1      | ["Acacia"]
//        HttpStatus.OK           | 0       | 1      | ["Acacia dfjghfdjhg"]
//    }

    def "name/check - POST #names causes error"() {
        when:
        HttpResponse response = nameService.bulkSearchNames(names)

        then:
        response.status() == status
        response.body()?.noOfResults == rm
        response.body()?.message == rm1

        where:
        status                  | rm1                                    | rm     | names
        HttpStatus.BAD_REQUEST  | "The list of names cannot be empty"    | null   | []
    }

    def "check validatenameString outcome - #name = #outString"() {
        when:
        String validatedString = nameService.validateNameString(name)

        then:
        validatedString == outString

        where:
        name | outString
        "hello"         | "hello"
        "someString%"   | null
        "f/oo"          | "f/oo"
        "-skdhf"        | "-skdhf"
    }

    def "check validateDatasetString outcome - #name = #outString"() {
        when:
        String validatedString = nameService.validateDatasetString(name)

        then:
        validatedString == outString

        where:
        name            | outString
        "hello"         | "hello"
        "apni%"         | null
        "a/ni"          | null
        "-apc"          | null
        "foo"           | "foo"
    }

    def "check buildMapInRightOrder() works for #records"() {
        when:
        List listofRecords = nameService.buildMapInRightOrder(records, "hello")

        then:
        Map record = listofRecords[0] as Map
        record.toString().indexOf("resultNameMatchType") == pos

        where:
        pos | records
        1   | [["scientificName": "hello"]]
        1   | [["scientificName": "some name", "rank": "some rank"]]

    }

    def "validateBulkNamePostData - check #names is #isValid"() {
        when:
        Boolean val = nameService.validateBulkNamePostData(names)

        then:
        val == isValid

        where:
        isValid  | names
        false    | ["names": ""]
        false    | ["name": ""]
        false    | ["names": []]
        true     | ["names": ["somename"]]
        true     | ["names": ["somename", "acacia", "doodia"]]
    }
}
