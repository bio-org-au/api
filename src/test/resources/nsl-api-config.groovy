nslapi {
    db {
        username = System.getenv('NSL_API_DB_USER')
        password = System.getenv('NSL_API_DB_PWD')
        url = "jdbc:${System.getenv('NSL_API_DB_URL')}"
        schema = System.getenv('NSL_API_DB_SCHEMA')
    }
    search {
        exactLimit = 5
        partialLimit = 50
    }
}