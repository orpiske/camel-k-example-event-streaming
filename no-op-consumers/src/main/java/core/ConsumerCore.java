package core;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;

public class ConsumerCore extends RouteBuilder {
    @PropertyInject("messaging.broker.url")
    String brokerUrl;

    public void configure() throws Exception {
        Sjms2Component sjms2Component = new Sjms2Component();
        sjms2Component.setConnectionFactory(new ActiveMQConnectionFactory(brokerUrl));
        getContext().addComponent("sjms2", sjms2Component);

        from("sjms2://queue:data.pm25.queue")
                .to("log:info Received PM25 data ${body}");
    }
}
