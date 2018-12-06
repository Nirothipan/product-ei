package org.wso2.carbon.esb.jms.transport.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.esb.integration.common.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.rmi.Naming;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class JMSEndpointTestCase extends ESBIntegrationTest {
    private EndPointAdminClient endPointAdminClient;
    private int consumerCount = 0;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadResource("/artifacts/ESB/jms/transport/jms_transport.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
        endPointAdminClient = new EndPointAdminClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }


    @Test(groups = {"wso2.esb"}, description = "Test JMS to JMS ")
    public void testJMSProxy() throws Exception {
        Thread.sleep(7000);

        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";
        try {
            sender.connect("JmsProxy");
            for (int i = 0; i < 3; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }
        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        Awaitility.await()
                  .pollInterval(200, TimeUnit.MILLISECONDS)
                  .atMost(300, TimeUnit.SECONDS)
                  .until(isMessageConsumed(consumer));
    }


    @AfterClass(alwaysRun = true)
    public void UndeployeService() throws Exception {
        super.init();
        endPointAdminClient = null;
        super.cleanup();
    }

    /**
     * Check if message contain in pop messages from consumer
     *
     * @param consumer
     * @return
     */
    private Callable<Boolean> isMessageConsumed(final JMSQueueMessageConsumer consumer) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    consumer.connect("SimpleStockQuoteService");
                    if (consumer.popMessage() == null) {
                        log.error("message is invalid");
                    } else {
                        log.info("Valid message received ");
                        consumerCount++;
                    }
                } catch (NamingException | JMSException | AutomationFrameworkException e) {
                    log.error("Error while reading message ", e);
                } finally {
                    consumer.disconnect();
                }
                if (consumerCount == 3) {
                    return true;
                }
                return false;
            }
        };
    }
}
