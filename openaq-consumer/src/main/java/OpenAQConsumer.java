import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class OpenAQConsumer extends RouteBuilder {

    public void configure() throws Exception {
        /*
         Read the data at a fixed interval of 1 second between each request, logging the execution of the
         route, setting up the HTTP method to GET and hitting the OpenAQ measurement API.
         */
        from("timer:refresh?period=10m&fixedRate=true")
                .log("OpenAQ route running")
                .setHeader(Exchange.HTTP_METHOD).constant("GET")
                .to("https://api.openaq.org/v1/measurements?limit=10000")
                .streamCaching()

                /*
                In this example we are only interested on the measurement data ... and we want to sent each
                measurement separately. To do, we use a splitter to split the results array and send to Kafka
                only the results data and nothing more.
                */
                .split().jsonpath("$.results[]")
                /*
                 Then setup a wireTap route to log the data before sending it to our Kafka instance.
                 */

                .wireTap("direct:tap")
                .to("kafka:pm-data?brokers={{kafka.bootstrap.address}}");

        from("direct:tap")
                .to("log:info");
    }
}
