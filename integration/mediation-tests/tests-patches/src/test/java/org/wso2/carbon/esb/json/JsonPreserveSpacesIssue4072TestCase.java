/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.json;

import java.io.IOException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

/**
 * This class tests whether when XML element value is only spaces, those spaces preserved when PRESERVE_SPACES
 * property is added. If property is not added, spaces should be escaped.
 */
public class JsonPreserveSpacesIssue4072TestCase extends ESBIntegrationTest {
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @BeforeClass()
    public void setEnvironment() throws Exception {
        super.init();
        verifyAPIExistence("JsonPreserveSpacesAPI");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.esb", description = "enabling preserving blank spaces in the XML payload")
    public void testPreservingSpacesProperty() throws Exception {
        testPreserveSpaces();
        testEscapeSpaces();
    }

    /**
     * Test with PRESERVE_SPACES property in the API. Spaces should be preserved.
     *
     * @throws IOException IO exception for http post request
     */
    private void testPreserveSpaces() throws IOException {
        String expectedValue = "{\"States\":{\"California\":12500,\"Florida\":\"        \",\"Texas\":\"Foo\"}}";
        String resPayload = getResPayload("http://localhost:8480/preserveSpacesProperty/preserveSpaces");
        Assert.assertEquals(expectedValue, resPayload);
    }

    /**
     * Test without PRESERVE_SPACES property in the API. Spaces should be escaped.
     *
     * @throws IOException IO exception for http post request
     */
    private void testEscapeSpaces() throws IOException {
        String expectedValue = "{\"States\":{\"California\":12500,\"Florida\":null,\"Texas\":\"Foo\"}}";
        String resPayload = getResPayload("http://localhost:8480/preserveSpacesProperty/escapeSpaces");
        Assert.assertEquals(expectedValue, resPayload);
    }

    /**
     * Get the response payload for a given url.
     *
     * @param url Url to send the post request.
     * @return Response payload
     * @throws IOException
     */
    private String getResPayload(String url) throws IOException {
        String payload = "<States>\n"
                + "\t<California>12500</California>\n"
                + "\t<Florida>        </Florida>\n"
                + "\t<Texas>Foo</Texas>\n"
                + "</States>";
        HttpResponse response = httpClient.doPost(url, null, payload, "application/xml");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        return new String(bos.toByteArray());
    }

    @AfterClass()
    public void destroy() throws Exception {
        super.cleanup();
    }
}
