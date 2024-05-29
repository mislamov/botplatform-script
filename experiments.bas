GLOBAL = {
    "telegram::6309244850": {"user_name": "Гульназ Федутинова",
                             "deamons": {"07.05.2024 19:00:54": {"trap_name": "Ловушка"},
                                         "07.05.2024 19:01:51": {"trap_name": "Демон"},
                                         "08.05.2024 10:27:36": {"trap_name": "Демон"},
                                         "08.05.2024 20:49:16": {"trap_name": "Ловушка", "topstandart": "agree",
                                                                 "pokornost": "agree", "nesostoyatel": "agree",
                                                                 "nepolnocen": "agree", "izgoy": "agree",
                                                                 "emo": "agree", "ujazvim": "agree", "zavisim": "noway",
                                                                 "nedoverie": "agree",
                                                                 "_Хочешь уменьшить влияние? Да": "21:06:16",
                                                                 "_Поблагодарить автора? Да": "21:06:45"},
                                         "result": {"otverzhenie": 2.0, "nedoverie": 5.0, "ujazvim": 6.0,
                                                    "zavisim": 5.0, "emo": 6.0, "izgoy": 6.0, "nepolnocen": 6.0,
                                                    "nesostoyatel": 6.0, "pokornost": 6.0, "topstandart": 6.0,
                                                    "izbran": 2.0}, "score": 8.0, "trap_name": "Ловушка"}}
}

print "g:" + GLOBAL["telegram::6309244850"].deamons.regexp[":"][-1]
print "s:" + GLOBAL["telegram::6309244850"].deamons.regexp[":"].size

// не ну а чо
gap = 5
while (gap > 2)
    gap -= 1
    print gap
end
