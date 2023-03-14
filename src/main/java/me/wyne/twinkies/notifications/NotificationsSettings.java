package me.wyne.twinkies.notifications;

public class NotificationsSettings {

    private boolean sendRegister = true;
    private boolean sendJoin = true;
    private boolean sendChangeNick = true;
    private boolean sendNewNick = true;
    private boolean sendDupeNick = true;
    private boolean sendChangeIp = true;
    private boolean sendNewIp = true;
    private boolean sendDupeIp = true;

    public boolean isSendRegister() {
        return sendRegister;
    }

    public void setSendRegister(boolean sendRegister) {
        this.sendRegister = sendRegister;
    }

    public boolean isSendJoin() {
        return sendJoin;
    }

    public void setSendJoin(boolean sendJoin) {
        this.sendJoin = sendJoin;
    }

    public boolean isSendChangeNick() {
        return sendChangeNick;
    }

    public void setSendChangeNick(boolean sendChangeNick) {
        this.sendChangeNick = sendChangeNick;
    }

    public boolean isSendNewNick() {
        return sendNewNick;
    }

    public void setSendNewNick(boolean sendNewNick) {
        this.sendNewNick = sendNewNick;
    }

    public boolean isSendDupeNick() {
        return sendDupeNick;
    }

    public void setSendDupeNick(boolean sendDupeNick) {
        this.sendDupeNick = sendDupeNick;
    }

    public boolean isSendChangeIp() {
        return sendChangeIp;
    }

    public void setSendChangeIp(boolean sendChangeIp) {
        this.sendChangeIp = sendChangeIp;
    }

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
