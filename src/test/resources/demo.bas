start:
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