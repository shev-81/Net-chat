# Net-chat - Сетевой чат

#### Проект на клиент - серверной архитектуре
- транспортная система обмена сообщениями основана на обмене объектами между клиентом и сервером, используется сериализация и десериализация объектов сообщений. (Java IO).
- клиентская часть написана с использованием Java FX + CSS. 
- подсистема аутентификации пользователя использует локальную файловую БД SQLite. для хранения данных пользователей.
- через стартовую страницу на клиенте можно пройти регистрацию и завести нового пользователя.
- взаимодействие с БД реализованно с использованием JDBC.   
- можно изменять свой статус для отображения в списке доступных пользователей в сети. (при изменении статуса он обновляется для всех пользователей On line).
- можно обмениваются сообщениями между собой в отдельных комнатах или в общей группе.
- есть одна общая группа, где все пользователи общаются со всеми. 
  (при необходимости заложен потенциал для реализации дополнительных групп пользователей).
- ведется сохранение истории общения пользователей на стороне клиента (в общей группе).

### В проекте 2 модуля клиент и сервер
- запускаем сервер в модуле Server. 
- запускаем сколько угодно клиентов через модуль Client
