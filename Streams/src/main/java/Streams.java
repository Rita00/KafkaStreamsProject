import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;


public class Streams {

    private static double negBalance = 99999999d;
    private static Long negId = 0l;

    public static String addToAggregator(Long aggKey, String newValue, String aggValue) {
        Long newClientId = Long.parseLong(newValue.split(",")[0]);
        Double newBalance = Double.parseDouble(newValue.split(",")[1]);

        Long oldClientId = Long.parseLong(aggValue.split(",")[0]);
        Double oldBalance = Double.parseDouble(aggValue.split(",")[1]);

        if(newBalance < 0 ){
            //System.out.println("Old balance" + oldBalance + " with Id " + oldClientId);
            if(newBalance < oldBalance){
              // System.out.println("New balance" + newBalance + " with Id " + newClientId);
                return (newClientId) + "," + (newBalance);
            }
        }
        return (oldClientId) + "," + (oldBalance);
    }

    public static Double convertCurrency(String jsonString) {

        JSONObject json = null;

        try {
            json = new JSONObject(jsonString);

            double value = Double.parseDouble(json.get("value").toString());
            double exchangeRate = Double.parseDouble(json.get("currencyExchangeRate").toString());
            double valEuros = value * exchangeRate;

            return valEuros;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String args[]) throws Exception {
        //Set topics names
        String cTopic = "Credits";
        String pTopic = "Payments";
        String windowedCreditPerClientTopic = "windowedCreditPerClient";
        String paymentsPerClientTopic = "paymentsperclient";
        String creditsPerClientTopic = "creditsperclient";
        String balancePerClientTopic = "balanceperclient";
        String totalResultsTopic = "totalresults";
        String mostNegBalanceTopic = "mostnegbalance";

        //Set properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exercises-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        //Streams builder
        StreamsBuilder builder = new StreamsBuilder();

        //---Topics subscriptions
        KStream<Long, String> creditsStream = builder.stream(cTopic);
        KStream<Long, String> paymentsStream = builder.stream(pTopic);

        System.out.println("Topics subscribed...");

        //---Credit per Client
        KTable<Long, Double> creditsPerClient = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("creditsperclient"));
        creditsPerClient
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"total_credits\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"total_credits\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(creditsPerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //---Payments Per Client
        KTable<Long, Double> paymentsPerClient = paymentsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("paymentsPerClient"));
        paymentsPerClient
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"total_payments\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"total_payments\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(paymentsPerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //---Balance per client
        ValueJoiner<Double, Double, Double> valueJoiner = ((payment, credit) -> payment - credit);
        KTable<Long, Double> joined = paymentsPerClient.join(creditsPerClient,
                valueJoiner
        );
        joined
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"current_balance\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"current_balance\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(balancePerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //---Sum every credit
        KTable<String, Double> dbCreditsTable = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupBy((k, v) -> "allCredits", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("aggregateCredits"));
        dbCreditsTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));


        //---Sum every payment
        KTable<String, Double> dbPaymentsTable = paymentsStream
                .mapValues((v) -> convertCurrency(v))
                .groupBy((k, v) -> "allPayments", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("aggregatePayments"));
        dbPaymentsTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));

        //---Sum every balance
        KStream<Long, Double> allActionsStream = builder.stream("allActions", Consumed.with(Serdes.Long(), Serdes.Double()));

        paymentsStream
                .mapValues((v) -> convertCurrency(v))
                .to("allActions", Produced.with(Serdes.Long(), Serdes.Double()));

        creditsStream
                .mapValues((v) -> -convertCurrency(v))
                .to("allActions", Produced.with(Serdes.Long(), Serdes.Double()));

        KTable<String, Double> dbBalancesTable = allActionsStream.
                groupBy((k, v) -> "allBalances", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum, Materialized.as("aggregateBalances"));

        dbBalancesTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));

        //---Total credit per client for the last month (tumbling window)
        Duration interval = Duration.ofDays(30);
        KTable<Windowed<Long>, Double> creditsPerClientMonthly = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .windowedBy(TimeWindows.of(interval))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("creditsPerClientMonthly"));
        creditsPerClientMonthly
                .toStream((wk, v) -> wk.key()).map((k, v) -> new KeyValue<>(k, v))
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"total_credits_lastmonth\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"total_credits_lastmonth\":" + v +
                                "}" +
                                "}"
                )
                .to(windowedCreditPerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //TO-DO
        //---Get clients without payments for the last 2 months

        //TO-DO
        //---Get the client with most negative balance
        joined
                .toStream()
                .map((k, v) -> new KeyValue<Long, String>(1l, k + "," + v))
                .groupByKey()
                .aggregate(
                        () -> "0,0",
                        (aggKey, newValue, aggValue) -> addToAggregator(aggKey, newValue, aggValue)
                )
                .toStream()
                .mapValues(v -> v.split(",")[0])
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"mostneg\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .to(mostNegBalanceTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //TO-DO
        //---(the highest sum of clients payments)

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        ArrayList<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic("allActions", 1, Short.parseShort("1")));
        AdminClient.create(props).createTopics(topics);
        streams.start();

        System.out.println("Reading stream from topic " + cTopic);
    }
}