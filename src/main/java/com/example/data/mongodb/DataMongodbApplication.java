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

import java.util.List;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import com.mongodb.client.MongoClient;

@SpringBootApplication
public class DataMongodbApplication implements Resource{

  	private static final Logger LOG = LoggerFactory.getLogger(DataMongodbApplication.class);

	private static final int VERSION = 1;

	@Autowired
	private MongoClient mongoClient;

	@Autowired
	private AuthorRepository authorRepository;

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(DataMongodbApplication.class, args);
	}

	public DataMongodbApplication() {
		Core.getGlobalContext().register(this);
	  }

	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {
		LOG.info("### V{}: Spring Context refreshed...", VERSION);
		insertTestdata();
	}
  
    @Override
	public void beforeCheckpoint(Context<? extends Resource> context) {
	  LOG.info("### V{}: CRaC's beforeCheckpoint callback method called...", VERSION);
	  LOG.info("### - Shutting down the MongoClient...");
	  mongoClient.close();
	  LOG.info("### - MongoClient closed.");
	}
  
	@Override
	public void afterRestore(Context<? extends Resource> context) {
	  LOG.info("### V{}: CRaC's afterRestore callback method called...", VERSION);
	  // insertTestdata();
	}

	private void insertTestdata() {
		LOG.info("### - Inserting some testdata in MongoDb...");
		authorRepository.saveAll(List.of(
		  new Author("id-1", "Brandon Sanderson"),
		  new Author("id-2", "Brent Weeks"),
		  new Author("id-3", "Peter V. Brett")));
	}
  
}
