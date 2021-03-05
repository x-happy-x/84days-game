from ImgTools import *

MAINPATH = "C:/Users/Абдулла/Desktop/Images/"
PROP = []

def packing():
	IMAGES = batchEditor(
		path=MAINPATH+"resultM",
		#savepath=MAINPATH+"resultM",
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
	cImgs = categorize(IMAGES)
	for i in cImgs.keys():
		createTexture(path=os.path.join(MAINPATH+"Atlas",i),img=cImgs[i], size=2048, prop=0)

def editImages():
	batchEditor(MAINPATH+"A/",MAINPATH+"A/",arg=[
		ImageFunc.SETPOS(os.path.join(MAINPATH,os.pardir,"POS/LEVEL 2/")),
		ImageFunc.TRIM,
		ImageFunc.ARESIZE(300,0)
	])
def editImages2():
	batchEditor(MAINPATH+"../B/LoadScreen/",MAINPATH+"../B/LoadScreen/result",arg=[
		ImageFunc.TRIM,
		ImageFunc.RESIZE(300,0)
	])

def dogAnim(path,size,h):
	imgs = MaxTrimming(path,arg=[
	ImageFunc.RESIZE(h,0)
	])
	for i in imgs.keys():
		createTexture(path=os.path.join(path,"atlas"),img=imgs[i],size=size, prop=False)

#dogAnim(MAINPATH+"../B/Dog/",2048,300)
#editImages()
img = batchEditor("","save/",arg=[
	ImageFunc.TRIM,
	ImageFunc.RESIZE(256,0)
],types=["png"])
s = sTexture("../menu/graphic.pref")
n = []
for i in img:
	n.append(i[1])
for i in s.Images:
	b = 1
	for j in n:
		if i[1].name == j.name:
			b = 0
			break
	if b:
		n.append(i[1])

createTexture(path="graphic.pref",img=n, size=2048, prop=0)

#editImages2()

#resizeTexture(MAINPATH+"ATLASES/LOADING/graphic.prop",sizes=[2048,1024,512])

#getImagesFromTexture(MAINPATH+"../B/graphic.pref")

#print("Удалено: ",clearEmptyDirs("/storage/emulated/0"))

"""T = Texture(MAINPATH)
T.addImage(Image(MAINPATH+"ATLASES/LOADING/ATLAS_IMG/1.png"))
T.addImage(Image(MAINPATH+"ATLASES/LOADING/ATLAS_IMG/1.png"))
T.getPositions()
T.draw()
T.addImage(Image(MAINPATH+"ATLASES/LOADING/ATLAS_IMG/1.png"))
T.addImage(Image(MAINPATH+"ATLASES/LOADING/ATLAS_IMG/1.png"))
T.getPositions(pics=T.imgList[-2:])
T.draw()
T.save()
"""