import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.qpid.jms.JmsConnectionFactory;

public class CamelCBR extends RouteBuilder {
    @PropertyInject("messaging.broker.url")
    String brokerUrl;

    public void configure() throws Exception {
        Sjms2Component sjms2Component = new Sjms2Component();
        sjms2Component.setConnectionFactory(new JmsConnectionFactory(brokerUrl));
        getContext().addComponent("sjms2", sjms2Component);

        from("kafka:pm-data?brokers={{kafka.bootstrap.address}}")
                .log("log:info received => ${body}")
                .streamCaching()
                .choice()
                .when(body().contains("parameter=pm10"))
                    .log("log:info PM10 Data = ${body}")
                    .to("sjms2://queue:data.pm10.queue")
                .when(body().contains("parameter=pm25"))
                    .log("log:info PM25 Data = ${body}")
                    .to("sjms2://queue:data.pm25.queue")
                .otherwise()
                    .log("log:info We don't need this data on this example = ${body}");
    }
}
