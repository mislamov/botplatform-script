''' проверяем есть ли замерщик в CRM по номеру тел
start:
    keyboard ["первый::onselect{selected=""a""}", "второй:onselect{selected=""b""}", "третий:onselect{selected=""c""}"]
    input any
    goto finish

onselect:
    print selected

finish:
    print "end"