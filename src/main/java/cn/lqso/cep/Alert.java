package cn.lqso.cep;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alert implements Serializable {
    private String deviceId;
    private Date startTime;
    private Date endTime;
    private long timeMinutes;
    private List<Float> values;

    @Override
    public String toString() {
        return "Alert{" +
                "deviceId='" + deviceId + '\'' +
                ", startTime=" + DateUtils.printTime(startTime) +
                ", endTime=" + DateUtils.printTime(endTime) +
                ", timeMinutes=" + timeMinutes +
                ", values=" + values +
                '}';
    }

    public long getTimeMinutes() {
        return timeMinutes;
    }

    public void setTimeMinutes(long timeMinutes) {
        this.timeMinutes = timeMinutes;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
