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

package org.eclipse.digitaltwin.basyx.aasservice.feature.mqtt;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.basyx.aasrepository.AasRepository;
import org.eclipse.digitaltwin.basyx.aasrepository.InMemoryAasRepositoryFactory;
import org.eclipse.digitaltwin.basyx.aasservice.AasServiceFactory;
import org.eclipse.digitaltwin.basyx.aasservice.AasServiceSuite;
import org.eclipse.digitaltwin.basyx.aasservice.DummyAssetAdministrationShell;
import org.eclipse.digitaltwin.basyx.aasservice.backend.InMemoryAasServiceFactory;
import org.eclipse.digitaltwin.basyx.common.mqttcore.encoding.URLEncoder;
import org.eclipse.digitaltwin.basyx.common.mqttcore.listener.MqttTestListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

public class TestMqttAasService extends AasServiceSuite {

	private static Server mqttBroker;
	private static MqttClient mqttClient;
	private static MqttTestListener listener;
	private static MqttAasServiceTopicFactory topicFactory = new MqttAasServiceTopicFactory(new URLEncoder());

	private static AasRepository aasRepository;
	private static AasServiceFactory mqttAasServiceFactory;

	private MqttAasService mqttAasService;
	private AssetAdministrationShell shell;

	@BeforeClass
	public static void setUpClass() throws MqttException, IOException {
		mqttBroker = startBroker();
		listener = configureInterceptListener(mqttBroker);
		mqttClient = createAndConnectClient();

		aasRepository = createMqttAasRepository();
		mqttAasServiceFactory = createMqttAasServiceFactory(mqttClient);

	}

	@Before
	public void setUp() {
		shell = DummyAssetAdministrationShell.getDummyShell();
		mqttAasService = (MqttAasService) getAASServiceFactory().create(shell);
	}

	@AfterClass
	public static void tearDownClass() {
		mqttBroker.removeInterceptHandler(listener);
		mqttBroker.stopServer();
	}

	@Override
	protected AasServiceFactory getAASServiceFactory() {
		return mqttAasServiceFactory;
	}

	private static AasServiceFactory createMqttAasServiceFactory(MqttClient client) {
		AasServiceFactory serviceFactory = new InMemoryAasServiceFactory();
		MqttAasServiceFeature mqttFeature = new MqttAasServiceFeature(client, aasRepository);
		return mqttFeature.decorate(serviceFactory);
	}

	@Test
	public void setAssetInformation() {
		AssetInformation assetInfo = createDummyAssetInformation();
		mqttAasService.setAssetInformation(assetInfo);
		String repoId = aasRepository.getName();

		assertEquals(topicFactory.createSetAssetInformationTopic(repoId, shell.getId()), listener.lastTopic);
		assertEquals(serialize(assetInfo), listener.lastPayload);
	}

	private AssetInformation createDummyAssetInformation() {
		AssetInformation assetInfo = new DefaultAssetInformation.Builder().assetKind(AssetKind.INSTANCE)
				.globalAssetID("assetIDTestKey")
				.build();
		return assetInfo;
	}

	@Test
	public void addSubmodelReferenceEvent() throws DeserializationException, JsonProcessingException {
		Reference submodelReference = DummyAssetAdministrationShell.submodelReference;
		mqttAasService.addSubmodelReference(submodelReference);
		String repoId = aasRepository.getName();

		assertEquals(topicFactory.createAddSubmodelReferenceTopic(repoId, shell.getId()), listener.lastTopic);
		assertEquals(serialize(submodelReference), listener.lastPayload);
	}

	private String serialize(Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException ignore) {
			ignore.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Test
	public void removeSubmodelReferenceEvent() throws DeserializationException, JsonProcessingException {
		String repoId = aasRepository.getName();

		DummyAssetAdministrationShell.addDummySubmodelReference(mqttAasService.getAAS());
		mqttAasService.removeSubmodelReference(DummyAssetAdministrationShell.SUBMODEL_ID);

		assertEquals(topicFactory.createRemoveSubmodelReferenceTopic(repoId, shell.getId()), listener.lastTopic);
		assertEquals(serialize(DummyAssetAdministrationShell.submodelReference), listener.lastPayload);
	}

	private static AasRepository createMqttAasRepository() {
		AasRepository repo = new InMemoryAasRepositoryFactory(mqttAasServiceFactory).create();
		return repo;
	}

	private static MqttClient createAndConnectClient() throws MqttException, MqttSecurityException {
		MqttClient client = new MqttClient("tcp://localhost:1884", "testClient");
		client.connect();
		return client;
	}

	private static MqttTestListener configureInterceptListener(Server broker) {
		MqttTestListener testListener = new MqttTestListener();
		broker.addInterceptHandler(testListener);

		return testListener;
	}

	private static Server startBroker() throws IOException {
		Server broker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();

		IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		broker.startServer(classPathConfig);

		return broker;
	}
}
