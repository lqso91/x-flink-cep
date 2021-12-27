package cn.lqso.cep;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GenerateTestData {
    private static final String[] deviceIds = new String[]{"D001", "D002", "D003"};

    public static final String filePath = "E:\\IdeaProjects\\x-flink-cep\\src\\main\\resources\\data\\voltage.tsv";

    public static void main(String[] args) throws IOException {
        FileOutputStream fos = FileUtils.openOutputStream(new File(filePath));

        DateTime start = DateTime.now().withDate(2021, 11, 1)
                .withTime(0, 0, 0, 0);
        while (start.isBefore(DateTime.now())) {
            for (String deviceId : deviceIds) {
                String time = start.toString("yyyy-MM-dd HH:mm:ss");
                System.out.println(time);

                String data = String.format("%s\t%s\t%.2f\t%.2f\t%.2f\n",
                        time,
                        deviceId,
                        RandomUtils.nextFloat(195, 249),
                        RandomUtils.nextFloat(195, 249),
                        RandomUtils.nextFloat(195, 249));
                IOUtils.write(data, fos, StandardCharsets.UTF_8);
            }
            start = start.plusMinutes(5);
        }

        IOUtils.close(fos);
    }
}
