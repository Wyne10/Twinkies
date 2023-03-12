package me.wyne.twinkies.notifications;

import me.wyne.twinkies.wconfig.ConfigField;

public class NotificationsConfig {

    @ConfigField(path = "player-info")
    private String playerInfo = "<blue><u>Нажмите, чтобы посмотреть информацию о игроке '%player_name%'";
    @ConfigField(path = "new-nick")
    private String newNick = "<red>Игрок '%player_name%' вошёл на сервер с новым никнеймом";
    @ConfigField(path = "new-ip")
    private String newIp = "<red>Игрок '%player_name%' вошёл на сервер с нового IP адреса: '%player_ip%'";
    @ConfigField(path = "dupe-ip")
    private String dupeIp = "<red>Игрок '%player_name%' вошёл на сервер с дублирующегося IP адреса: '%player_ip%'" +
                            "<br>Этот IP уже был замечен у игрока %player_dupe%";

    public String getPlayerInfo() {
        return playerInfo;
    }

    public String getNewNick() {
        return newNick;
    }

    public String getNewIp() {
        return newIp;
    }

    public String getDupeIp() {
        return dupeIp;
    }
}
