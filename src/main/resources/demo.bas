start:
    print "Приветствую!\n\nЯ создам для вас бота, который сам опросит лидов и сохранит их со всеми ответами в ваш Битрикс24.\n\nВсё максимально просто - никаких конструкторов и программирования!\n\nОтвечайте на мои вопросы, следуйте моим инструкциям и уже через несколько минут у вас будет свой бот, который автоматизирует прием заявок."
    xxx = 2 + 0.2
    inline ["Мне нужен бот", "Отмена" + xxx, [1,2,3]]
    input cmd
    if cmd = "Мне нужен бот" then newbot
    goto start

newbot:
    print "Какое сообщение бот должен отправить клиенту в самом начале беседы - перед ответами на вопросы? Это может быть приветствие, описание услуги и/или краткая интсрукция:"
    input welcomeText

    print "Далее заполните пункты вашего опросника. По одному вопросу на одно отдельное сообщение. Сообщения можно редактировать при необходимости. Когда опросник будет заполнен - нажмите на /finish"
    button "/finish"
_askquestion:
    input cmd
    if cmd = "/finish" then _questionresult
    push questions cmd
    goto askquestion
_questionresult:
    print "questions.size = " + questions.size
    if questions.size > 0 then _bitrix
    print "Укажите хотя бы один вопрос. /finish - завершить ввод вопросов"
    goto _askquestion

_bitrix:
    print "Отлично. Теперь настроим подключение к Битрикс и при желании создадим в нем тестового лида."
_bitrixiput:
    print "Введите ссылку на ваш битрикс:"
    input bitrixLink
    bitrix = EXEC "VERIFYBITRIX" bitrixLink

    if bitrix.error = NULL then _bitrixsuccess
    print "Не удалось распознать ссылку на битрикс: " + bitrix.error + "\nСледуйте инструкции: https://youtube.com/video"
    goto _bitrixiput
_bitrixsuccess:

    print "Остался один шаг! Регистрация бота в Telegram"
_botfather:
    print "1. Напишите боту @BotFather команду /newbot"
    print "2. Введите заголовок для вашего бота в диалоге с @BotFather"
    print "3. Подберите свободное имя боту на латинице. Имя должно заканчиваться на 'bot'"
    print "4. Перешлите итоговое сообщение от @BotFather с именем бота и токеном в этот диалог"

    input botFatherMessage
    botResult = EXEC "VERIFYBOT" botFatherMessage
    if botResult.error == NULL then bye
    print botResult.error
    goto _botfather

bye:
    print "На этом всё! Создайте своего первого лида в своем новом боте"
    print welcomeText + questions
