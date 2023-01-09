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
import groovy.sql.GroovyResultSet
import groovy.sql.ResultSetMetaDataWrapper
import groovy.sql.Sql
import groovy.sql.GroovyRowResult
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

    ApiTaxonView getRow() {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
                select * from api.api_taxon_view limit 5;
            ''')
            if (row) {
                ApiTaxonView apiTaxonView = new ApiTaxonView(this, row)
                return apiTaxonView
            } else {
                return null
            }
        }
    }

    List<ApiTaxonView> getRows() {
        List rows = []
        withSql { Sql sql ->
            sql.eachRow('''
                select * from api.api_taxon_view limit 5;
            ''') {row ->
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

    public <T> T withSql(Closure<T> work) {
        Sql sql = Sql.newInstance(dataSource)
        try {
            return work(sql)
        } finally {
            sql.close()
        }
    }
}
