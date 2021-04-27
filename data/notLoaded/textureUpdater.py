from ImgTools import *

def updateLevel1():
    pass

def updateLevel2():
    imgs = batchEditor(
		path="textures/level2/",
		savepath="save/resultM2",
		arg=[
			#ImageFunc.SETPOS(os.path.join(MAINPATH,os.pardir,"POS/LEVEL 2/")),
			#ImageFunc.ADDPROP('pic',ImageFunc._FILENAME_),
			#ImageFunc.ADDPROP('id',ImageFunc._FILEID_),
			#ImageFunc.GETPROP(PROP),
			#ImageFunc.TRIM,
			#ImageFunc.RESIZE(128,0),
			#ImageFunc.SHADOW(3,10,(0,0,0,70)),
			#ImageFunc.ARESIZE(300,0),
			ImageFunc.ADD_MARGIN(5)
		],
		types=["png"],
		r=False,
		otherPaths=False
	)
    imgs = categorize(imgs)
    for i in imgs.keys():
        createTexture(path=os.path.join("save/"+"Atlas",i),img=imgs[i], size=2048, prop=0)

def unpackAll(path_):
    for path, dirs, files in os.walk(path_):
        for i in files:
            try:
                x = sTexture(os.path.join(path,i))
                x.saveImages(os.path.abspath(os.path.join("save/save",path,os.path.splitext(i)[0])))
                print("Текстура... ",os.path.join("save",path,i))
            except:
                print("Другой файл...", i)

def tmpUpdate(fn,height=246,margin=5):
    x = Image("notLoaded/"+fn)
    x.trim()
    x.setSize(height=height)
    x.addMargin(margin)
    x.save(path="notLoaded/cache",fileName=fn)

def menuUpdate():
    a = ["square_darkgray_btn.png","gray1_btn.png",
    "gray2_btn.png","gray3_btn.png","gray4_btn.png"]
    for i in a:
        tmpUpdate(i)
    img = batchEditor("save/menu/graphic",types=["png","jpg"])
    img = categorize(img)[""]
    for i in a:
        img.append(Image("cache/"+i))
    createTexture("textures",size=2048,img=img,atlasName="graphic")
    
def bgUpdate():
    a = ["loadbg.png","note.png"]
    tmpUpdate(a[1],300,0)
    tmpUpdate(a[0],720,0)
    img = []
    for i in a:
        img.append(Image("notLoaded/cache/"+i))
    createTexture("notLoaded/textures",size=2048,img=img,atlasName="load",padding=5);
#menuUpdate()
bgUpdate()
#getImagesFromTexture("")