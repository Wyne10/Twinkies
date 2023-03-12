package me.wyne.twinkies.notifications;

public class NotificationsSettings {

    private boolean sendNewIp = true;
    private boolean sendDupeIp = true;

    public boolean isSendNewIp() {
        return sendNewIp;
    }

    public void setSendNewIp(boolean sendNewIp) {
        this.sendNewIp = sendNewIp;
    }

    public boolean isSendDupeIp() {
        return sendDupeIp;
    }

    public void setSendDupeIp(boolean sendDupeIp) {
        this.sendDupeIp = sendDupeIp;
    }
}
