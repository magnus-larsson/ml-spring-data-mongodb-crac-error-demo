/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.data.mongodb;

import com.mongodb.client.MongoClient;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataMongodbApplication implements Resource{

  private static final Logger LOG = LoggerFactory.getLogger(DataMongodbApplication.class);

	@Autowired
	private MongoClient mongoClient;

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(DataMongodbApplication.class, args);
	}

	public DataMongodbApplication() {
		Core.getGlobalContext().register(this);
	  }

	@Override
	public void beforeCheckpoint(Context<? extends Resource> context) {
	  LOG.info("### V3: CRaC's beforeCheckpoint callback method called...");
	  LOG.info("### - Shutting down the MongoClient...");
	  mongoClient.close();
	  LOG.info("- MongoClient closed.");
	}
  
	@Override
	public void afterRestore(Context<? extends Resource> context) {
	  LOG.info("### V3: CRaC's afterRestore callback method called...");
  //    LOG.info("### MongoDb: " + mongodDbHost + ":" + mongodDbPort);
	}
  
}
