package au.org.biodiversity.nslapi

import au.org.biodiversity.nslapi.services.ReaderService

import java.lang.reflect.Modifier

class ApiTaxonView {
    ReaderService readerService

    String taxonID
    String nameType
    String acceptedNameUsageID
    String acceptedNameUsage
    String nomenclaturalStatus
    String taxonomicStatus
    String proParte
    String scientificName
    String scientificNameID
    String canonicalName
    String scientificNameAuthorship
    String parentNameUsageID
    String taxonRank
    String taxonRankSortOrder
    String kingdom
    String classs
    String subclass
    String family
    String created
    String modified
    String datasetName
    String taxonConceptID
    String nameAccordingTo
    String nameAccordingToID
    String taxonRemarks
    String taxonDistribution
    String higherClassification
    String firstHybridParentName
    String firstHybridParentNameID
    String secondHybridParentName
    String secondHybridParentNameID
    String nomenclaturalCode
    String license
    String ccAttributionIRI

    ApiTaxonView(ReaderService readerService, Map data) {
        this.readerService = readerService
        if (!data) {
            throw new NullPointerException("No data supplied to create an object of ${this.getClass()}")
        } else {
            this.taxonID = data.taxonID
            this.nameType = data.nameType
            this.acceptedNameUsageID = data.acceptedNameUsageID
            this.acceptedNameUsage = data.acceptedNameUsage
            this.nomenclaturalStatus = data.nomenclaturalStatus
            this.taxonomicStatus = data.taxonomicStatus
            this.proParte = data.proParte
            this.scientificName = data.scientificName
            this.scientificNameID = data.scientificNameID
            this.canonicalName = data.canonicalName
            this.scientificNameAuthorship = data.scientificNameAuthorship
            this.parentNameUsageID = data.parentNameUsageID
            this.taxonRank = data.taxonRank
            this.taxonRankSortOrder = data.taxonRankSortOrder
            this.kingdom = data.kingdom
            this.classs = data.class            // note the field name is a keyword
            this.subclass = data.subclass
            this.family = data.family
            this.created = data.created
            this.modified = data.modified
            this.datasetName = data.datasetName
            this.taxonConceptID = data.taxonConceptID
            this.nameAccordingTo = data.nameAccordingTo
            this.nameAccordingToID = data.nameAccordingToID
            this.taxonRemarks = data.taxonRemarks
            this.taxonDistribution = data.taxonDistribution
            this.higherClassification = data.higherClassification
            this.firstHybridParentName = data.firstHybridParentName
            this.firstHybridParentNameID = data.firstHybridParentNameID
            this.secondHybridParentName = data.secondHybridParentName
            this.secondHybridParentNameID = data.secondHybridParentNameID
            this.nomenclaturalCode = data.nomenclaturalCode
            this.license = data.license
            this.ccAttributionIRI = data.ccAttributionIRI
        }
    }

    @Override
    String toString() {
        """ Object of ApiTaxonView -> 
        taxonID: $taxonID,
        nomenclaturalCode: $nomenclaturalCode, datasetName: $datasetName,
        nameType: $nameType, taxonomicStatus: $taxonomicStatus,
        taxonRank: $taxonRank, scientificName: $scientificName,
        higherClassification: $higherClassification
        """
    }

    Map asMap() {
        Map result = this.class.declaredFields.findAll({
            it.modifiers == Modifier.PRIVATE
        }).collectEntries({
            [ (it.name):this[it.name] ]
        })
        result.remove('readerService')
        result
    }
}
