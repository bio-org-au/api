nslapi {
    db {
        username = "hasura"
        password = "hasura"
        url = "jdbc:postgresql://localhost:5432/api-test"
        schema = "api"
    }
    search {
        exactLimit = 5
        partialLimit = 50
    }
    graphql {
	    url = "http://localhost:8080/v1/graphql"
        adminSecret: "admin"
    }
}