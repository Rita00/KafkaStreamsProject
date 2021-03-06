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

//Streams application
public class Streams {
    //Aggregator operation
    public static String addToAggregator(String newValue, String aggValue) {
        //Aggregate key can be ignored because is always 1 as there is only one item at a time

        //Get from the new string value the client and balance
        long newClientId = Long.parseLong(newValue.split(",")[0]);
        double newBalance = Double.parseDouble(newValue.split(",")[1]);

        //aggValue has the last highest debt (most negative balance) and the correspondent client
        long oldClientId = Long.parseLong(aggValue.split(",")[0]);
        double oldBalance = Double.parseDouble(aggValue.split(",")[1]);

        //If the client with the highest debt remains the same but the balance has changed
        if (newClientId == oldClientId && newBalance > oldBalance && newBalance < 0) {
            //Update it
            return (newClientId) + "," + (newBalance);
        } else if (newClientId == oldClientId && newBalance > oldBalance && newBalance > 0)
            return "0,0";

        //Debt is only if balance is negative
        if (newBalance < 0) {
            //If the new balance is more negative than the old balance
            if (newBalance < oldBalance) {
                //Update it
                return (newClientId) + "," + (newBalance);
            }
        }

        //Otherwise, keep the old values
        return (oldClientId) + "," + (oldBalance);
    }

    //Convert a given currency to euros
    public static Double convertCurrency(String jsonString) {

        //Initialize json object
        JSONObject json;

        try {
            //Convert string from clients to json object
            json = new JSONObject(jsonString);

            //From object get
            //Value (the actual amount of the credit/payment)
            double value = Double.parseDouble(json.get("value").toString());
            //Exchange rate from the credit/payment currency to euros
            double exchangeRate = Double.parseDouble(json.get("currencyExchangeRate").toString());
            //The object also has the currency name which is unused

            //Return value times the exchange rate to get value in euros
            return value * exchangeRate;
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("\nSomething went wrong while converting to json!");
            //Something went wrong, return -1, so it can be debugged
            return -1d;
        }
    }

    public static Long extractLongField(String jsonString, String field) {
        try {
            JSONObject j = new JSONObject(jsonString);
            try {
                return new JSONObject(j.get("payload").toString()).getLong(field);
            } catch (JSONException e) {
                return j.getLong(field);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        //Set topics names
        String cTopic = "Credits";
        String pTopic = "Payments";
        String windowedCreditPerClientTopic = "windowedcreditperclient";
        String paymentsPerClientTopic = "paymentsperclient";
        String creditsPerClientTopic = "creditsperclient";
        String balancePerClientTopic = "balanceperclient";
        String totalResultsTopic = "totalresults";
        String mostNegBalanceTopic = "mostnegbalance";
        String bestRevenue = "bestrevenue";
        String noPaymentsTopic = "nopayments";
        String dbClients = "DBInfoTopics";

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

        //---Credit per Client
        KTable<Long, Double> creditsPerClient = creditsStream
                .mapValues(Streams::convertCurrency)
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce(Double::sum, Materialized.as("creditsperclient"));
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
                .mapValues(Streams::convertCurrency)
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce(Double::sum, Materialized.as("paymentsPerClient"));
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
        KStream<Long, Double> allActionsStream = builder.stream("allActions", Consumed.with(Serdes.Long(), Serdes.Double()));
        allActionsStream
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2)

//
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
                .mapValues(Streams::convertCurrency)
                .groupBy((k, v) -> "allCredits", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum, Materialized.as("aggregateCredits"));
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
                .mapValues(Streams::convertCurrency)
                .groupBy((k, v) -> "allPayments", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum, Materialized.as("aggregatePayments"));
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
        paymentsStream
                .mapValues(Streams::convertCurrency)
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
                .mapValues(Streams::convertCurrency)
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .windowedBy(TimeWindows.of(interval))
                .reduce(Double::sum, Materialized.as("creditsPerClientMonthly"));
        creditsPerClientMonthly
                .toStream((wk, v) -> wk.key()).map(KeyValue::new)
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


        //---Get clients without payments for the last 2 months
        Duration intervalTwoMonths = Duration.ofDays(60);

        KStream<Long, String> streamClients = builder.stream(dbClients, Consumed.with(Serdes.Long(), Serdes.String()))
                .map((k, v) -> new KeyValue<Long, String>(extractLongField(v, "id"), extractLongField(v, "id").toString()));

        ValueJoiner<String, String, String> valueJoinerNoPayments = ((clientValue, paymentValue) -> {
            if (paymentValue != null)
                return "paidInLastTwoMonths";
            return null;
        });
        streamClients.leftJoin(paymentsStream,
                        valueJoinerNoPayments,
                        JoinWindows.of(intervalTwoMonths)
                )
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
                                "\"type\":\"boolean\"," +
                                "\"optional\":false," +
                                "\"field\":\"paid\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"paid\":" + (v != null) +
                                "}" +
                                "}"
                )
                .to(noPaymentsTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //---Get the client with most negative balance
        allActionsStream
                .groupByKey()
                .reduce((v1, v2) -> v1 + v2)
                .toStream()
                .map((k, v) -> new KeyValue<>(1L, k + "," + v))
                .groupByKey()
                .aggregate(
                        () -> "0,0",
                        (aggKey, newValue, aggValue) -> addToAggregator(newValue, aggValue)
                )
                .toStream()
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"primary_key\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"current_balance\"" +
                                "}," +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"mostneg\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"primary_key\":\"highestDebt\"," +
                                "\"client_id\":" + v.split(",")[0] +
                                ", \"current_balance\":" + v.split(",")[1] +
                                "}" +
                                "}"
                )
                .to(mostNegBalanceTopic, Produced.with(Serdes.Long(), Serdes.String()));


        //---(the highest sum of clients payments)
        paymentsStream
                .map((k, v) -> new KeyValue<>(extractLongField(v, "manager_id"), convertCurrency(v)))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce(Double::sum)
                .toStream()
                .map((k, v) -> new KeyValue<>(k, v + "," + k))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.String()))
                .aggregate(
                        () -> "0,0",
                        (aggKey, newValue, aggValue) -> {

                            long newManagerId = Long.parseLong(newValue.split(",")[1]);
                            double newManagerRevenue = Double.parseDouble(newValue.split(",")[0]);

                            double bestManagerRevenue = Double.parseDouble(aggValue.split(",")[0]);
                            if (newManagerRevenue > bestManagerRevenue) {
                                return newManagerRevenue + "," + newManagerId;
                            }
                            return aggValue;
                        }
                )
                .toStream()
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"primary_key\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"revenue\"" +
                                "}," +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"manager_id\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"bestrevenue\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"primary_key\":\"highestRevenue\"," +
                                "\"manager_id\":" + v.split(",")[1] +
                                ", \"revenue\":" + v.split(",")[0] +
                                "}" +
                                "}"
                )
                .to(bestRevenue, Produced.with(Serdes.Long(), Serdes.String()));

        //Create streams with the previously set properties
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        //Create intermediate topic needed for sum of every balance
        ArrayList<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic("allActions", 1, Short.parseShort("1")));
        AdminClient.create(props).createTopics(topics);

        //Start streaming
        streams.start();
    }
}