package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.ApiTaxonView
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class ReaderServiceImplSpec extends Specification {
    @Inject
    ReaderService readerService

    def "buildSql - 1 param"() {
        when: " Calling with just one param"
        String s = readerService.buildSql("Eukaryota")

        then: "Only the value is set with other default values"
        s == "SELECT * FROM api.taxa WHERE \"scientificName\" ILIKE \'Eukaryota\';"
    }

    def "buildSql - 2 params"() {
        when: "Only two params are passed"
        String s = readerService.buildSql("blah", "canonicalName")

        then: "Both values are set in the SQL that is returned"
        s == "SELECT * FROM api.taxa WHERE \"canonicalName\" ILIKE \'blah\';"
    }

    def "buildSql - 3 params"() {
        when: "All three params are passed"
        String s = readerService.buildSql("IBIS", "abc", 10)

        then: "All three values are set in the SQL that is returned"
        s == "SELECT * FROM api.taxa WHERE \"abc\" ILIKE \'IBIS\';"
    }

    def "getRow null data throws exception"() {
        when:
        new ApiTaxonView(readerService, null)

        then:
        thrown(NullPointerException)
    }

    def "getRow asMap returns a Map"() {
        when:
        String s = readerService.buildSql("Anthozoa")
        ApiTaxonView row = readerService.getRow(s)

        then:
        row.asMap().getClass() == LinkedHashMap
        row.toString().contains("Object of ApiTaxonView -> ")
    }

    def "getRow function returns a row #names - #tr #nc"() {
        when:
        String s = readerService.buildSql(names)
        ApiTaxonView row = readerService.getRow(s)

        then:
        sn == row.scientificName
        nc == row.nomenclaturalCode
        tr == row.taxonRank
        names << ["Eukaryota", 'Anthozoa', 'Cymbella aspera (Ehrenb.) Cleve', 'Cymbella gastroides (Kütz) Bréb. & Godey', 'Bacillariophyceae classis incertae sedis']

        where:
        names                                       || sn                       | nc        | tr
        "Eukaryota"                                 || "Eukaryota"              | "ICN"     | "Regnum"
        'Anthozoa'                                  || "Anthozoa"               | "ICN"     | "Division"
        'Eucocconeis Cleve'                         || "Eucocconeis Cleve"            | "ICN"     | "Genus"
        'Cymbella gastroides (Kütz) Bréb. & Godey'  || "Cymbella gastroides (Kütz) Bréb. & Godey" | "ICN"     | "Species"
        'Bacillariophyceae classis incertae sedis'  || 'Bacillariophyceae classis incertae sedis' | "ICN"     | "Classis"
    }

    def "getRows function returns a row #names - #size"() {
        when:
        String s = readerService.buildSql(names)
        List<ApiTaxonView> rows = readerService.getRows(s)

        then:
        size == rows.size()
        names << ["Eukaryota", 'Anthozoa', 'Cymbella aspera (Ehrenb.) Cleve', 'Cymbella gastroides (Kütz) Bréb. & Godey', 'Bacillariophyceae classis incertae sedis']

        where:
        names                                       || size
        "Eukaryota"                                 || 3
        'Cymbella aspera (Ehrenb.) Cleve'           || 1
        'Cymbella gastroides (Kütz) Bréb. & Godey'  || 1
        'Bacillariophyceae classis incertae sedis'  || 1
    }
}
