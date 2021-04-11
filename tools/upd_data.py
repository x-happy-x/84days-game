from zipfile import ZipFile
from ftplib import FTP
from server import *
import os

# Локальные параметры
version = "1"
app_version = "1"
state = "1"
message = "None"
directory = "D:/Projects/Game/data"
game = "D:/Projects/Game/tools"
black_list_dir = ["notLoaded","cache","updates"]
black_list_file = ["prefs","version.txt","lastmoded.txt"]
lastmoded_file = os.path.join(game,black_list_file[-1])
info_file = os.path.join(game,black_list_file[-2])
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
text = ""

version = int(version)+1
update_small_pack = os.path.join(game, "updates", "upd_%d.zip" % version)
update_full_pack  = os.path.join(game, "updates", "upd_full.zip")

with open(os.path.join(game,"info.php"),"w") as f:
    f.write(f"""<?php\n\t$app_version = {app_version};\n\t$version = {version};\n\t$state = {state};\n\t$message = "{message}";\n?>""")

lastmoded = {}
if os.path.exists(lastmoded_file):
    with open(lastmoded_file,"r") as f:
        t = f.read()
        for i in t.split("\n"):
            ii = i[::-1].split(" ",1)
            lastmoded[ii[1][::-1]] = float(ii[0][::-1])

x = ZipFile(update_full_pack+"_temp","w")
z = ZipFile(update_small_pack+"_temp","w")
for root, dirs, files in os.walk(directory):
    b = False
    for i in black_list_dir:
        if root.replace(directory,"",1)[1:].startswith(i):
            b = True
            break
    if b:
        continue
    for file in files:
        if os.path.join(root.replace(directory,"",1),file) in black_list_file:
            continue
        x.write(os.path.join(root,file),os.path.join(root.replace(directory,"",1),file))
        if os.path.join(root,file) in lastmoded.keys() and lastmoded[os.path.join(root,file)] >= os.path.getmtime(os.path.join(root,file)):
            continue
        lastmoded[os.path.join(root,file)] = os.path.getmtime(os.path.join(root,file))
        z.write(os.path.join(root,file),os.path.join(root.replace(directory,"",1),file))

if len(z.infolist()) > 0:
    # Локальное сохранение
    print("Содержимое архива:", z.filename)
    z.printdir()
    x.close()
    z.close()
    if os.path.exists(update_full_pack): os.remove(update_full_pack)
    if os.path.exists(update_small_pack): os.remove(update_small_pack)
    os.rename(update_full_pack+"_temp",update_full_pack)
    os.rename(update_small_pack+"_temp",update_small_pack)
    # Загрузка на сервер
    ftp = FTP()
    ftp.connect(HOST,PORT)
    ftp.login(USER,PASS)
    ftp.cwd("domains/"+HOST_SITE+"/updates")
    print("Загрузка",update_full_pack)
    ftp.storbinary("STOR full_pack.zip",open(update_full_pack,"rb"))
    print("Загрузка",update_small_pack)
    ftp.storbinary("STOR upd_%d.zip" % version,open(update_small_pack,"rb"))
    ftp.storbinary("STOR update_info.php",open(os.path.join(game,"info.php"),"rb"))
    ftp.quit()
    # Запись файла с временем последней модификации файлов
    with open(lastmoded_file,"w") as f:
        t = ""
        for i in lastmoded.keys():
            t += i + " " + str(lastmoded[i])+"\n"
        f.write(t.strip())
    with open(os.path.join(directory,"menu","game.pref"),"r") as f:
        t = f.read().split("\n")
    for i in range(len(t)):
        if "version:" in t[i]:
            t[i] = "version: "+str(version)
    with open(os.path.join(directory,"menu","game.pref"),"w") as f:
        f.write("\n".join(t))
    # Обновление версии данных на сервере
    with open(info_file,"w") as f:
        f.write(f"# Версия приложения\n{app_version}\n# Версия данных\n{version}\n# Состояние\n{state}\n# Сообщение при состоянии -1\n{message}")
else:
    # В полученных архивах нет изменений
    print("Изменений нет")
    z.close()
    x.close()
    os.remove(update_full_pack+"_temp")
    os.remove(update_small_pack+"_temp")

input("Нажмите ENTER чтобы завершить")