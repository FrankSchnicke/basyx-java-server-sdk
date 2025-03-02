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

package org.eclipse.digitaltwin.basyx.http.serialization;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Supports the tests working with the HTTP/REST API of AAS, Submodels, etc.
 * 
 * @author schnicke
 *
 */
public class BaSyxHttpTestUtils {

	/**
	 * Reads the JSON String from a JSON file in the classpath
	 * 
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readJSONStringFromClasspath(String fileName) throws FileNotFoundException, IOException {
		ClassPathResource classPathResource = new ClassPathResource(fileName);
		InputStream in = classPathResource.getInputStream();
		return IOUtils.toString(in, StandardCharsets.UTF_8.name());
	}

	/**
	 * Retrieves the String content of the HttpResponse
	 * 
	 * @param retrievalResponse
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static String getResponseAsString(CloseableHttpResponse retrievalResponse) throws IOException, ParseException {
		return EntityUtils.toString(retrievalResponse.getEntity(), "UTF-8");
	}

	/**
	 * Asserts that two JSON strings are semantically equivalent
	 * 
	 * @param expected
	 * @param actual
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public static void assertSameJSONContent(String expected, String actual) throws JsonProcessingException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();

		assertEquals(mapper.readTree(expected), mapper.readTree(actual));
	}

	/**
	 * Performs a get request on the passed URL
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static CloseableHttpResponse executeGetOnURL(String url) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet getRequest = createGetRequestWithHeader(url);
		return client.execute(getRequest);
	}

	/**
	 * Performs a delete request on the passed URL
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static CloseableHttpResponse executeDeleteOnURL(String url) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpDelete deleteRequest = createDeleteRequestWithHeader(url);
		return client.execute(deleteRequest);
	}

	/**
	 * Performs a set request on the passed URL with the passed content
	 * 
	 * @param url
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static CloseableHttpResponse executePutOnURL(String url, String content) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPut putRequest = createPutRequestWithHeader(url, content);

		return client.execute(putRequest);
	}

	/**
	 * Performs a post request on the passed URL with the passed content
	 * 
	 * @param url
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static CloseableHttpResponse executePostOnURL(String url, String content) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost aasCreateRequest = createPostRequest(url, content);

		return client.execute(aasCreateRequest);
	}

	/**
	 * Performs a patch request on the passed URL with the passed content
	 * 
	 * @param url
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static CloseableHttpResponse executePatchOnURL(String url, String content) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPatch patchRequest = createPatchRequestWithHeader(url, content);

		return client.execute(patchRequest);
	}

	private static HttpPatch createPatchRequestWithHeader(String url, String content) {
		HttpPatch patchRequest = new HttpPatch(url);

		patchRequest.setHeader("Content-type", "application/json");
		patchRequest.setEntity(new StringEntity(content));

		return patchRequest;
	}

	private static HttpPut createPutRequestWithHeader(String url, String content) {
		HttpPut putRequest = new HttpPut(url);

		putRequest.setHeader("Content-type", "application/json");
		putRequest.setEntity(new StringEntity(content));

		return putRequest;
	}

	private static HttpPost createPostRequest(String url, String content) {
		HttpPost aasCreateRequest = createPostRequestWithHeader(url);

		StringEntity aasEntity = new StringEntity(content);
		aasCreateRequest.setEntity(aasEntity);

		return aasCreateRequest;
	}

	private static HttpPost createPostRequestWithHeader(String url) {
		HttpPost aasCreateRequest = new HttpPost(url);
		aasCreateRequest.setHeader("Content-type", "application/json");
		aasCreateRequest.setHeader("Accept", "application/json");
		return aasCreateRequest;
	}

	private static HttpGet createGetRequestWithHeader(String url) {
		HttpGet aasCreateRequest = new HttpGet(url);
		aasCreateRequest.setHeader("Content-type", "application/json");
		aasCreateRequest.setHeader("Accept", "application/json");
		return aasCreateRequest;
	}

	private static HttpDelete createDeleteRequestWithHeader(String url) {
		HttpDelete deleteRequest = new HttpDelete(url);
		deleteRequest.setHeader("Content-type", "application/json");
		return deleteRequest;
	}
}
