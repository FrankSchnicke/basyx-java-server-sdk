/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/

package org.eclipse.digitaltwin.basyx.conceptdescriptionrepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * ConceptDescriptionRepository factory returning a MongoDb backend
 * ConceptDescriptionRepository
 * 
 * @author schnicke, danish, kammognie
 */
@Component
@ConditionalOnExpression("'${basyx.backend}'.equals('MongoDB')")
public class MongoDBConceptDescriptionRepositoryFactory implements ConceptDescriptionRepositoryFactory {

	private MongoTemplate mongoTemplate;
	private String collectionName;
	private String cdRepositoryName;

	@Autowired(required = false)
	public MongoDBConceptDescriptionRepositoryFactory(MongoTemplate mongoTemplate,
			@Value("${basyx.conceptdescriptionrepository.mongodb.collectionName:conceptdescription-repo}") String collectionName) {
		this.mongoTemplate = mongoTemplate;
		this.collectionName = collectionName;
	}

	@Autowired(required = false)
	public MongoDBConceptDescriptionRepositoryFactory(MongoTemplate mongoTemplate,
			@Value("${basyx.conceptdescriptionrepository.mongodb.collectionName:conceptdescription-repo}") String collectionName,
			@Value("${basyx.cdrepo.name:cd-repo}") String cdRepositoryName) {
		this(mongoTemplate, collectionName);
		this.cdRepositoryName = cdRepositoryName;
	}

	@Override
	public ConceptDescriptionRepository create() {
		return new MongoDBConceptDescriptionRepository(mongoTemplate, collectionName, cdRepositoryName);
	}
}
