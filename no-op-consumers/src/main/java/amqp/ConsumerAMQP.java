package amqp;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.qpid.jms.JmsConnectionFactory;

public class ConsumerAMQP extends RouteBuilder {
    @PropertyInject("messaging.broker.url")
    String brokerUrl;

    public void configure() throws Exception {
        Sjms2Component sjms2Component = new Sjms2Component();
        sjms2Component.setConnectionFactory(new JmsConnectionFactory(brokerUrl));
        getContext().addComponent("sjms2", sjms2Component);

        from("sjms2://queue:data.pm10.queue")
                .to("log:info Received PM10 data ${body}");
    }
}
