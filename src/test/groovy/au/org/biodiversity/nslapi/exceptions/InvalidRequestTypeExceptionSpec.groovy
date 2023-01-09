package au.org.biodiversity.nslapi.exceptions

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class InvalidRequestTypeExceptionSpec extends Specification {

    def "Check constructor"() {
        when:
        def customException = new InvalidRequestTypeException("custom message")

        then:
        customException.getMessage() == "custom message"
    }
}
