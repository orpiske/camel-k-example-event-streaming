package service;

import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.spi.PropertiesComponent;

public class UserReportSystem extends RouteBuilder {

    public static class Data {
        public static class User {
            private String name;
            private String token;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getToken() {
                return token;
            }

            public void setToken(String token) {
                this.token = token;
            }
        }

        public static class Report {
            private String type;
            private String measurement;
            private String value;
            private String location;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getMeasurement() {
                return measurement;
            }

            public void setMeasurement(String measurement) {
                this.measurement = measurement;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }
        }

        private User user;
        private Report report;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public Report getReport() {
            return report;
        }

        public void setReport(Report report) {
            this.report = report;
        }
    }

    public void configure() throws Exception {
        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")
                .port("8080");

        rest("/")
                .get("/report/list").to("direct:report-list")
                .put("/report/new").to("direct:report-new");

        from("direct:report-list")
                .transform().constant("Not implemented");

        JacksonDataFormat reportFormat = new JacksonDataFormat();
        reportFormat.setUnmarshalType(Data.class);

        from("direct:report-new")
                .streamCaching()
                .wireTap("direct:audit")
                .unmarshal(reportFormat)
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        PropertiesComponent pc = getContext().getPropertiesComponent();

                        String[] userList = pc.loadProperties().getProperty("users.allowed").split(",");

                        Data data = exchange.getMessage().getBody(Data.class);

                        if (Arrays.asList(userList).contains(data.getUser().getName())) {
                            exchange.getMessage().setHeader("authorized", true);
                            exchange.getMessage().setBody("Authorized");
                            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                        }
                        else {
                            exchange.getMessage().setHeader("authorized", false);
                            exchange.getMessage().setBody("Unauthorized");
                            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
                        }
                    }
                });


        from("direct:audit")
                .to("knative:channel/audit");

        from("direct:log")
                .convertBodyTo(String.class)
                .to("log:info");

    }
}
