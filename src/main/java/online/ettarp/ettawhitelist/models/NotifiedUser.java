package online.ettarp.ettawhitelist.models;

public class NotifiedUser {

    private String  uuid;
    private Boolean isWeeklyNotified;
    private Boolean isThreeDaysNotified;
    private Boolean isOneDayNotified;

    public NotifiedUser(String uuid, Boolean isWeeklyNotified, Boolean isThreeDaysNotified, Boolean isOneDayNotified) {
        this.uuid = uuid;
        this.isWeeklyNotified = isWeeklyNotified;
        this.isThreeDaysNotified = isThreeDaysNotified;
        this.isOneDayNotified = isOneDayNotified;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setWeeklyNotified(Boolean weeklyNotified) {
        isWeeklyNotified = weeklyNotified;
    }

    public void setThreeDaysNotified(Boolean threeDaysNotified) {
        isThreeDaysNotified = threeDaysNotified;
    }

    public void setOneDayNotified(Boolean oneDayNotified) {
        isOneDayNotified = oneDayNotified;
    }

    public String getUuid() {
        return uuid;
    }

    public Boolean getWeeklyNotified() {
        return isWeeklyNotified;
    }

    public Boolean getThreeDaysNotified() {
        return isThreeDaysNotified;
    }

    public Boolean getOneDayNotified() {
        return isOneDayNotified;
    }
}
