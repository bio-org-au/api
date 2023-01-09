/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL API project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

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
        adminSecret = "admin"
    }
}