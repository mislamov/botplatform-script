q = 2
max_q = 100
base_menu = ["a", "b", "c"]

    tail_menu = []
    if q > 1 then tail_menu += "Назад::go_back"
    if q < max_q then tail_menu += "Вперёд::go_forward"
    current_menu = base_menu + [tail_menu]
    print current_menu
    debug current_menu



    m.inline = [
        [ "ДА 👍::{answ." +q+ "=5}", "Скорее да::{answ." +q+ "=4}", "Не знаю::{answ." +q+ "=3}", "Скорее нет::{answ." +q+ "=2}", "НЕТ 👎::{answ." +q+ "=1}" ],
        ["Назад", "Вперёд"]
        ]
    print m

start:
    x = (2 + 2 * print(1))

    v = """
    frame string x = $x
    """

    a.b.c = "abc"
    var.subvar.subsub = [1,2,3, a]
    print "var: {" + var + "}"
    print "[1,2,3] = " + var.subvar.subsub
    print "1 = " + var.subvar.subsub.0
    print "2 = " + var.subvar.subsub.1
    var.subvar.subsub.1 = 22
    var.subvar.subsub.8 = 99
    print "22 = " + var.subvar.subsub.1
    print "a.b = " + var.subvar.subsub.3.b
    print "var: {" + var + "}"

    keyboard ["первый::onselect{selected=""a""}", "второй:onselect{selected=""b""}", "третий:onselect{selected=""c""}"]
    'input any
    print "any: " + any
    goto finish

onselect:
    print "onselect: " + selected

finish:
    print "end"