import java.time.LocalTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PollutionBridge extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PollutionBridge.class);

    @PropertyInject("messaging.broker.url")
    String brokerUrl;

    public static class PollutionData {
        public static class DateInfo {
            private LocalTime utc;
            private LocalTime local;

            public LocalTime getUtc() {
                return utc;
            }

            public void setUtc(LocalTime utc) {
                this.utc = utc;
            }

            public LocalTime getLocal() {
                return local;
            }

            public void setLocal(LocalTime local) {
                this.local = local;
            }
        }

        public static class Coordinates {
            private double longitude;
            private double latitude;

            public double getLongitude() {
                return longitude;
            }

            public void setLongitude(double longitude) {
                this.longitude = longitude;
            }

            public double getLatitude() {
                return latitude;
            }

            public void setLatitude(double latitude) {
                this.latitude = latitude;
            }
        }

        /*
        value=8, unit=?g/m?, coordinates={longitude=116.8709, latitude=38.3228}, country=CN, city=???}
     */

        private String location;
        private String parameter;
        private DateInfo date;
        private double value;
        private String unit;
        private Coordinates coordinates;
        private String country;
        private String city;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }

        public DateInfo getDate() {
            return date;
        }

        public void setDate(DateInfo date) {
            this.date = date;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }


    public void configure() throws Exception {
        Sjms2Component sjms2Component = new Sjms2Component();
        sjms2Component.setConnectionFactory(new JmsConnectionFactory(brokerUrl));
        getContext().addComponent("sjms2", sjms2Component);

        JacksonDataFormat dataFormat  = new JacksonDataFormat();
        dataFormat.setUnmarshalType(PollutionData.class);

        from("kafka:pm-data?brokers={{kafka.bootstrap.address}}")
                .unmarshal(dataFormat)
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        PollutionData pollutionData = exchange.getMessage().getBody(PollutionData.class);
                        LOG.info("Processing pollution data for city {} " + pollutionData.getCity());
                    }
                })
                .log("log:info Pollution Data = ${body}");

        from("kafka:earthquake-data?brokers={{kafka.bootstrap.address}}")
                .log("log:info received => ${body}")
                .streamCaching()
                .log("log:info Earthquake Data = ${body}");

    }
}
