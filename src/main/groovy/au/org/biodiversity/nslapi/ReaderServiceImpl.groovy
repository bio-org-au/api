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
package au.org.biodiversity.nslapi

import groovy.sql.Sql
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.sql.DataSource

@Slf4j
@CompileStatic
@Singleton
@Requires(beans = DataSource.class)
class ReaderServiceImpl implements  ReaderService {
    @Inject
    DataSource dataSource

    Tuple<Tree> getTree() {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
                select * from tree;
            ''')
            if (row) {
                Tree tree = new Tree(this, row)
//                println("Tree -> ${tree.toString()}")
                Tuple<Tree> newTree = new Tuple<Tree>(tree)
                println("New Tree: ${newTree.acceptedTree}")
                return newTree
            } else {
                return null
            }
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
