package au.org.biodiversity.nslapi.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class NameServiceImplSpec extends Specification {
    @Inject
    NameService nameService

    def "name/check - #name matches #rm records"() {
        when:
        HttpResponse response = nameService.checkAndProcess(name, 'APNI')

        then:
        response.status() == status
        response.body().verbatimSearchString == ss
        response.body().noOfResults == rm

        where:
        status          | rm            | name                  | ss
        HttpStatus.OK   | 0             | "hello"               | "hello"
        HttpStatus.OK   | 2             | "Acacia dealbata"     | "Acacia dealbata"
        HttpStatus.OK   | 1             | "Acacia"              | "Acacia"
    }
}
