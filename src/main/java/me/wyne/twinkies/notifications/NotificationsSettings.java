package me.wyne.twinkies.notifications;

import me.wyne.wutils.settings.Setting;
import me.wyne.wutils.settings.WSettings;

public class NotificationsSettings extends WSettings {

    @Setting(setMessage = "Уведомления о регистрации")
    private boolean sendRegister = true;
    @Setting(setMessage = "Уведомления о присоединении")
    private boolean sendJoin = true;
    @Setting(setMessage = "Уведомления о смене ника")
    private boolean sendChangeNick = true;
    @Setting(setMessage = "Уведомления о новом нике")
    private boolean sendNewNick = true;
    @Setting(setMessage = "Уведомления о дублирующемся нике")
    private boolean sendDupeNick = true;
    @Setting(setMessage = "Уведомления о смене IP")
    private boolean sendChangeIp = true;
    @Setting(setMessage = "Уведомления о новом IP")
    private boolean sendNewIp = true;
    @Setting(setMessage = "Уведомления о дублирующемся IP")
    private boolean sendDupeIp = true;

}
