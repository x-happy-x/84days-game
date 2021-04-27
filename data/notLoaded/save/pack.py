from ImgTools import *

def packing():
	IMAGES = batchEditor(
		path="",
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
			#ImageFunc.ADD_MARGIN(5)
		],
		types=["png"],
		r=True,
		otherPaths=False
	)
	cImgs = categorize(IMAGES)
	for i in cImgs.keys():
		createTexture(path=os.path.join("../packed",i),img=cImgs[i], size=2048, prop=0, padding=5)

packing()