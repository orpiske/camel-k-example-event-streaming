import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class EarthquakeConsumer extends RouteBuilder {

    public void configure() throws Exception {
/*
         Read the data at a fixed interval of 1 second between each request, logging the execution of the
         route, setting up the HTTP method to GET and hitting the OpenAQ measurement API.
         */
        from("timer:refresh?period=10m&fixedRate=true")
                .log("USGS Earthquake route running")
                .setHeader(Exchange.HTTP_METHOD).constant("GET")
                .to("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson")
                .streamCaching()

                /*
                In this example we are only interested on the measurement data ... and we want to sent each
                measurement separately. To do, we use a splitter to split the results array and send to Kafka
                only the results data and nothing more.
                */
                .split().jsonpath("$.features[*]")
                /*
                 Then setup a wireTap route to log the data before sending it to our Kafka instance.
                 */

                .wireTap("direct:tap")
                .to("kafka:earthquake-data?brokers={{kafka.bootstrap.address}}");

        from("direct:tap")
                .to("log:info");
    }
}