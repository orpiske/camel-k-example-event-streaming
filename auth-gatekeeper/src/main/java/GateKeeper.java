import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class GateKeeper extends RouteBuilder {

    public void configure() throws Exception {
        from("knative:channel/authentication")
                .streamCaching()
                .convertBodyTo(String.class)
                .wireTap("direct:log")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getMessage().setBody("Authorized :)");
                    }
                });

        from("direct:log")
                .to("log:info");
    }
}
