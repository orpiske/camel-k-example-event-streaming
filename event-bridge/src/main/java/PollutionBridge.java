import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.qpid.jms.JmsConnectionFactory;

public class PollutionBridge extends RouteBuilder {
    @PropertyInject("messaging.broker.url")
    String brokerUrl;

    public void configure() throws Exception {
        Sjms2Component sjms2Component = new Sjms2Component();
        sjms2Component.setConnectionFactory(new JmsConnectionFactory(brokerUrl));
        getContext().addComponent("sjms2", sjms2Component);

        from("kafka:pm-data?brokers={{kafka.bootstrap.address}}")
                .log("log:info Pollution Data = ${body}");

        from("kafka:earthquake-data?brokers={{kafka.bootstrap.address}}")
                .log("log:info received => ${body}")
                .streamCaching()
                .log("log:info Earthquake Data = ${body}");

    }
}
