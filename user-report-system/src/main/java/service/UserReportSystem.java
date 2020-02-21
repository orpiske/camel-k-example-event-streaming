package service;

import org.apache.camel.builder.RouteBuilder;

public class UserReportSystem extends RouteBuilder {

    public void configure() throws Exception {
//        from("platform-http:/report/new?httpMethodRestrict=POST")
//                .to("log:info ${body}");

        restConfiguration()
                .host("0.0.0.0")
                .port("8080");

        rest("/report")
                .post("/new").to("direct:report-new");

        from("direct:report-new")
                .streamCaching()
                .wireTap("log:info ${body}").end()
                .transform().constant("Hello from knative:)");

    }
}
