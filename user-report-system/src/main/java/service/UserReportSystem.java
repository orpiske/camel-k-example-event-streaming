package service;

import org.apache.camel.builder.RouteBuilder;

public class UserReportSystem extends RouteBuilder {

    public void configure() throws Exception {
        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")
                .port("8080");

        rest("/")
                .get("/report/list").to("direct:get-root")
                .put("/report/new").to("direct:report-new");

        from("direct:get-root")
                .transform().constant("Nothing to see here, move along ;) ");

        from("direct:report-new")
                .streamCaching()
                .wireTap("direct:log").end()
                .to("knative:channel/authentication");
// For debugging
//                .transform().constant("Hello from knative :)");

        from("direct:log")
                .convertBodyTo(String.class)
                .to("log:info");

    }
}
