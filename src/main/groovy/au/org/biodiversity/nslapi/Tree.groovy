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

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.json.JsonSlurper

class Tree {
    @JsonIgnore
    ReaderService readerService

    Long id
    Long lockVersion
    Boolean acceptedTree
    Map config
    Long currentTreeVersionId
    Long default_draft_tree_version_id
    String description_html
    String group_name
    String host_name
    String link_to_home_page
    String name
    String reference_id

    Tree(ReaderService readerService, Map data) {
        def slurper = new JsonSlurper()
        this.readerService = readerService
        if (!data) {
            throw new NullPointerException("Cannot create a tree without any data")
        }
        this.id = data.id
        this.acceptedTree = data.accepted_tree
        this.host_name = data.host_name
        this.config = slurper.parseText(data.config.toString())
    }

//    @Override
//    String toString() {
//        return "Tree $id -> Accepted $acceptedTree :: Config: $config"
//    }
}
