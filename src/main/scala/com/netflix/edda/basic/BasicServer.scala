/*
 *
 *  Copyright 2012 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.edda.basic;

import com.netflix.edda.aws.AwsBeanMapper
import com.netflix.edda.aws.AwsCollectionBuilder
import com.netflix.edda.CollectionManager
import com.netflix.edda.mongo.MongoDatastore
import com.netflix.edda.mongo.MongoElector

import javax.servlet.http.HttpServlet

import org.slf4j.{Logger, LoggerFactory}

class BasicServer extends HttpServlet {
    private[this] val logger = LoggerFactory.getLogger(getClass)
    
    override
    def init = {
        logger.info("Staring Server");
        val dsFactory = (name: String) => Some(new MongoDatastore(BasicContext, name))
        val elector = new MongoElector(BasicContext)
        
        val bm = new BasicBeanMapper(BasicContext) with AwsBeanMapper
        AwsCollectionBuilder.buildAll(BasicContext, bm, elector, dsFactory).foreach(
            pair => {
                CollectionManager.register(pair._1, pair._2)
            }
        )
        logger.info("Starting Collections");
        CollectionManager.start

        super.init
    }

    override 
    def destroy = {
        CollectionManager.stop
        super.destroy
    }
}