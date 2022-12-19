start:
    var.subvar.subsub = [1,2,3]
    print "var: {" + var + "}"
    print "[1,2,3] = " + var.subvar.subsub

    keyboard ["первый::onselect{selected=""a""}", "второй:onselect{selected=""b""}", "третий:onselect{selected=""c""}"]
    input any
    print "any: " + any
    goto finish

onselect:
    print "onselect: " + selected

finish:
    print "end"