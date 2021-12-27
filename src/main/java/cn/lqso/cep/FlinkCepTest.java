package cn.lqso.cep;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.EventComparator;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FlinkCepTest
 *
 * @author luojie
 * @since 2021/12/27
 */
public class FlinkCepTest {
    public static void main(String[] args) throws Exception {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("org.apache.flink").setLevel(Level.valueOf("ERROR"));

        // create environment
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(1);

        // create data stream
        SingleOutputStreamOperator<Voltage> input = environment.addSource(new SourceFunction<Voltage>() {
                    final String[] deviceIds = new String[]{"D001", "D002", "D003"};

                    @Override
                    public void run(SourceContext<Voltage> context) throws Exception {
                        DateTime start = DateTime.now().withDate(2021, 12, 20)
                                .withTime(0, 0, 0, 0);
                        while (start.isBefore(DateTime.now())) {
                            for (String deviceId : deviceIds) {
                                Voltage voltage = new Voltage();
                                voltage.setTime(start.toDate());
                                voltage.setDeviceId(deviceId);
                                voltage.setA(RandomUtils.nextFloat(195, 280));
                                voltage.setB(RandomUtils.nextFloat(195, 280));
                                voltage.setC(RandomUtils.nextFloat(195, 280));
                                context.collectWithTimestamp(voltage, voltage.getTime().getTime());
                            }
                            start = start.plusMinutes(5);
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                })
                .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());

        float overVoltage = 242f;

        // cep pattern
        Pattern<Voltage, ?> pattern = Pattern.<Voltage>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<Voltage>() {
                    @Override
                    public boolean filter(Voltage voltage) {
                        return voltage.getA() > overVoltage/* || voltage.getB() > overVoltage || voltage.getC() > overVoltage*/;
                    }
                })
                .oneOrMore()
                .consecutive()
                .followedBy("end")
                .where(new SimpleCondition<Voltage>() {
                    @Override
                    public boolean filter(Voltage voltage) {
                        return voltage.getA() < overVoltage;
                    }
                });

        PatternStream<Voltage> patternStream = CEP.pattern(input.keyBy(Voltage::getDeviceId), pattern, (EventComparator<Voltage>) (o1, o2) -> Long.compare(o1.getTime().getTime(), o2.getTime().getTime()));

        SingleOutputStreamOperator<Alert> select = patternStream.select((PatternSelectFunction<Voltage, Alert>) patternMap -> {
            List<Voltage> start = patternMap.get("start");

            Alert alert = new Alert();
            Voltage first = start.get(0);
            Voltage last = start.get(start.size() - 1);

            alert.setDeviceId(first.getDeviceId());
            alert.setStartTime(first.getTime());
            alert.setEndTime(last.getTime());
            alert.setTimeMinutes((last.getTime().getTime() - first.getTime().getTime()) / 1000 / 60);
            alert.setValues(start.stream().map(Voltage::getA).collect(Collectors.toList()));

            return alert;
        });

        select.print();
        environment.execute();
    }
}
