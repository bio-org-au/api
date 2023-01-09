-- Create schema
CREATE SCHEMA IF NOT EXISTS api;
GRANT USAGE ON SCHEMA api TO hasura;
-- Create table with name taxa in api schema
CREATE TABLE api.taxa (
    "taxonID" varchar(255),
    "nameType" varchar(255),
    "acceptedNameUsageID" varchar(255),
    "acceptedNameUsage" varchar(255),
    "nomenclaturalStatus" varchar(255),
    "taxonomicStatus" varchar(255),
    "proParte" varchar(255),
    "scientificName" varchar(255),
    "scientificNameID" varchar(255),
    "canonicalName" varchar(255),
    "scientificNameAuthorship" varchar(255),
    "parentNameUsageID" varchar(255),
    "taxonRank" varchar(255),
    "taxonRankSortOrder" varchar(255),
    "kingdom" varchar(255),
    "class" varchar(255),
    "subclass" varchar(255),
    "family" varchar(255),
    "created" timestamp with time zone,
    "modified" timestamp with time zone,
    "datasetName" varchar(255),
    "taxonConceptID" varchar(255),
    "nameAccordingTo" varchar(255),
    "nameAccordingToID" varchar(255),
    "taxonRemarks" varchar(255),
    "taxonDistribution" varchar(255),
    "higherClassification" varchar(255),
    "firstHybridParentName" varchar(255),
    "firstHybridParentNameID" varchar(255),
    "secondHybridParentName" varchar(255),
    "secondHybridParentNameID" varchar(255),
    "nomenclaturalCode" varchar(255),
    "license" varchar(255),
    "ccAttributionIRI" varchar(255)
);
GRANT SELECT ON api.taxa TO hasura;