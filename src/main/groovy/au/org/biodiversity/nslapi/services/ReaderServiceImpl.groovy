/*
    Copyright (c) 2021 Australian National Botanic Gardens and Authors

    This file is part of National Species List project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package au.org.biodiversity.nslapi.services

import au.org.biodiversity.nslapi.ApiTaxonView
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import javax.sql.DataSource
/*
    Implementation of the ReaderService
* */

@Slf4j
@CompileStatic
@Singleton
@Requires(beans = DataSource.class)
class ReaderServiceImpl implements  ReaderService {
    @Inject
    DataSource dataSource

    /**
     * Get multiple rows for the SQL passed and return a List of
     * ApiTaxonView objects
     * @param String sql
     * @return List of ApiTaxonView objects
     */
    ApiTaxonView getRow(String s) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow(s)
            if (row) {
                ApiTaxonView apiTaxonView = new ApiTaxonView(this, row)
                return apiTaxonView
            } else {
                return null
            }
        }
    }

    /**
     * Get multiple rows for the SQL passed and return a List of
     * ApiTaxonView objects
     * @param String sql
     * @return List of ApiTaxonView objects
     */
    List<ApiTaxonView> getRows(String s) {
        log.debug("Running SQL: $s")
        List rows = []
        withSql { Sql sql ->
            sql.eachRow(s) {row ->
                def md = row.getMetaData()
                Map rowMap = [:]
                for (i in 1..md.getColumnCount()) {
                    rowMap.put(md.getColumnLabel(i), row[i-1])
                }
                rows.add(rowMap)
            }
            return rows
        }
    }

    /**
     * Function to run SQL and return process the results using generic types
     * @param Closure work
     * @return Generic Type
     */
    public <T> T withSql(Closure<T> work) {
        Sql sql = Sql.newInstance(dataSource)
        try {
            return work(sql)
        } finally {
            sql.close()
        }
    }

    /**
     * Build SQL query with and without wild card to get data from the taxa
     * @param String searchString,
     *        String column to match,
     *        Boolean rightWildCrd
     * @return String
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    public String buildSql(String value, String column = "scientificName", Integer limit = 5) {
            return "SELECT * FROM api.taxa WHERE \"$column\" ILIKE \'$value\';"
    }
}
