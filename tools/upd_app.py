from ftplib import FTP
from server import *
import os, re

# Локальные параметры
version = "1"
app_version = "1"
state = "1"
message = "None"
app = "D:/Projects/Game/android/build/outputs/apk/debug/android-debug.apk"
game = "D:/Projects/Game/tools"
info_file = os.path.join(game,"version.txt")

# Обновление локальных данных
if os.path.exists(info_file):
    t = ""
    with open(info_file,"r") as f:
        t = f.read()
    upd_data = []
    for i in t.split("\n"):
        if i.startswith("#"):
            continue
        upd_data.append(i)
    if len(upd_data) > 4:
        upd_data = upd_data[0],upd_data[1],upd_data[2],"\n".join(upd_data[3:])
    if len(upd_data) == 4:
        app_version, version, state, message = upd_data

maingdx_file = 'D:/Projects/Game/core/src/ru/happy/game/adventuredog/MainGDX.java'
text = ""
with open(maingdx_file,"r",encoding="utf-8") as f:
	text = f.read()
app_version = int(re.findall(r"APP_VERSION = \d*;", text, re.MULTILINE)[0].replace("APP_VERSION = ","").replace(";",""))
	

with open(os.path.join(game,"info.php"),"w") as f:
    f.write(f"""<?php\n\t$app_version = {app_version};\n\t$version = {version};\n\t$state = {state};\n\t$message = "{message}";\n?>""")
# Загрузка на сервер
ftp = FTP()
ftp.connect(HOST,PORT)
ftp.login(USER,PASS)
ftp.cwd("domains/"+HOST_SITE+"/updates/apk")
print("Загрузка",app)
ftp.storbinary("STOR app%d.apk"%app_version,open(app,"rb"))
ftp.cwd("..")
ftp.storbinary("STOR update_info.php",open(os.path.join(game,"info.php"),"rb"))
ftp.quit()
with open(info_file,"w") as f:
    f.write(f"# Версия приложения\n{app_version}\n# Версия данных\n{version}\n# Состояние\n{state}\n# Сообщение при состоянии -1\n{message}")

app_version+=1
text = re.sub(r"APP_VERSION = \d*;", "APP_VERSION = "+str(app_version)+";", text, 0, re.MULTILINE)
with open(maingdx_file,"w",encoding="utf-8") as f:
	f.write(text)