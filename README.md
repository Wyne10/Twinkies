# Twinkies
![GitHub](https://img.shields.io/badge/Version-1.2-green)
![GitHub](https://img.shields.io/badge/Paper-1.19.2%2B-lightgrey)
![GitHub](https://img.shields.io/github/license/Wyne10/Twinkies)

Twinkies - это маленький плагин для Paper 1.19.2+ позволяющий отслеживать твинки игроков.

## Требования
 - Paper 1.19.2+
 - PlaceholderAPI

## Возможности
 - Сохранение истории никнеймов и IP адресов игроков
 - Отправка уведомлений для администрации сервера
 - Индивидуальная настройка уведомлений для каждого администратора
 - Полное логирование уведомлений и сообщений плагина и запись в файл с логами
 - Определение смены ника и IP игрока, а также определение дублирующихся ников и IP
 - Возможность манипулирования сохранёнными данными о игроках через команды
 - Логирование, сохранение данных, выполнение команд, отправка уведомлений происходят в разных потоках
 - Возможность предугадывать похожие никнеймы игроков

## Права
 - twinkies.*
 - twinkies.notifications: Позволяет игроку получать уведомления и изменять их настройки
 - twinkies.playerData: Позволяет игроку просматривать сохранённую информацию
   - twinkies.playerDataMod: Позволяет игроку удалять сохранённую информацию
 - twinkies.logging: Позволяет игроку изменяит настройки логирования (Однако чтобы настройки сохранились после перезагрузки плагина, нужно изменить их в config файле)
 - twinkies.bypass: Не будет отправлять уведомления о твинках этого игрока

## Команды
- twinkies
- - twinkies reload - Перезагрузить конфиг (Доступен только для ОП)
- - twinkies logging [logName] - Настройка логирования (twinkies.logging)
- - twinkies notif [notifName] - Настройка уведомлений (twinkies.notifications)
- - twinkies data
- - - twinkies data search - Поиск твинков всех игроков
- - - twinkies data player
- - - - twinkies data player [playerNick] - Показать информацию о игроке [playerNick]
- - - - twinkies data player [playerNick] delete - Удалить всю информацию о игроке [playerNick]
- - - - twinkies data player [playerNick] nick [nick] - Найти твинки игрока [playerNick] по указанному [nick]
- - - - twinkies data player [playerNick] nick [nick] delete - Удалить указанный [nick] игрока [playerNick]
- - - - twinkies data player [playerNick] ip [ip] - Найти твинки игрока [playerNick] по указанному [ip]
- - - - twinkies data player [playerNick] ip [ip] delete - Удалить указанный [ip] игрока [playerNick]
- - - - twinkies data player [playerNick] search - Найти твинки игрока [playerNick] по всем сохранённым никнеймам и IP
- - - twinkies data nick
- - - - twinkies data nick [nick] - Найти игроков использовавших указанный [nick]
- - - twinkies data ip
- - - - twinkies data ip [ip] - Найти игроков использовавших указанный [ip]