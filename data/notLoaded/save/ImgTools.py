#pylint:disable=W0102
from PIL import Image as PIMG,ImageFilter, ImageDraw, ImageOps
from random import randint
import os, re, numpy as np, shutil, time, copy

############   Работа с файлами    #############
# Файловый менеджер (хрень)
class FileManager:
	files = {}
	filters = []
	PATH,FULLPATH,POSITION,RESULT,CROPED,RESIZED,ATLAS="path","full","position","result","croped","resized","atlas"
	def __init__(self, path, filters = False, otherPaths = False):
		self.files[self.PATH] = [os.path.realpath(path),[],[]]
		if otherPaths:
			self.files[self.FULLPATH] = [os.path.realpath(os.path.join(path,self.FULLPATH)),[],[]]
			self.files[self.POSITION] = [os.path.realpath(os.path.join(path,self.POSITION)),[],[]]
			self.files[self.RESULT] = [os.path.realpath( os.path.join(path,os.pardir,self.RESULT,path.split('/')[-1])),[],[]]
			self.files[self.CROPED] = [os.path.realpath(os.path.join(self.files[self.RESULT][0],self.CROPED)),[],[]]
			self.files[self.RESIZED] = [os.path.realpath(os.path.join(self.files[self.RESULT][0],self.RESIZED)),[],[]]
			self.files[self.ATLAS] = [os.path.realpath(os.path.join(self.files[self.RESULT][0],self.ATLAS)),[],[]]
		if filters:
			self.filters = filters
		self.update()
		
	def update(self):
		for i in self.files.keys():
			self.mkDirs(self.files[i][0])
			tmp = sorted(os.listdir(self.files[i][0]))
			dirs, files = [],[]
			for j in tmp:
				if os.path.isdir(os.path.join(self.files[i][0],j)):
					dirs.append(j)
				elif os.path.isfile and (j.split('.')[-1] in self.filters or len(self.filters) == 0):
					files.append(j)
			self.files[i][1:] = dirs,files
			
	def addPath(self, name, path):
		self.files[name] = [os.path.realpath(path),[],[]]
		self.update()

	def getPath(self, path):
		return self.files[path][0]
	
	def getDirs(self, path):
		return self.files[path][1]
		
	def getFiles(self, path):
		return self.files[path][2]
		
	def getParentDir(self, path):
		return os.path.join(os.path.realpath(path),os.pardir)
	
	def mkDirs(self, path):
		if not os.path.exists(path):
			os.makedirs(path)

# Получение всех файлов
def getAllFiles(path):
	r = os.walk(path)
	rr = []
	for i in r:
		rr.append(i)
	return rr

# Очистка пустых папок
def clearEmptyDirs(path):
	clearing = True
	x = 0
	while clearing:
		r = os.walk(path)
		rr = []
		for i in r:
			rr.append(i)
		deleted = 0
		clearing = False
		for address, dirs, files in rr:
			if len(files) + len(dirs) == 0:
				#clear()
				print("Путь:",address[len(path):],"\nпапок:",len(dirs),"файлов:",len(files),"\n")
				os.rmdir(address)
				clearing = True
				deleted += 1
		x += deleted
	return x

# Логически правильная сортировка файлов с числами в названии
def sortingDir(path):
	if type(path) is list:
		files = path
	else:
		files = os.listdir(path)
	nfiles = []
	for i in files:
		b = ""
		m = re.findall('(\d+)', i)
		if len(m) == 0:
			nfiles.append([i,i])
			continue
		for j in range(len(m)):
			if j == 0:
				b+=i[:i.index(m[j])]
			else:
				b+=i[i.index(m[j-1])+1:i.index(m[j])]
			b+="0"*(10-len(m[j]))+m[j]
		b += i[i.index(m[-1])+len(m[-1]):]
		nfiles.append([i,b])
	return list(map(lambda x: x[0],sorted(nfiles, key=lambda x: x[1])))

# Открытие файла
def getFile(path, encode = "utf-8"):
       with open(path,"r",encoding=encode) as f:
       	return f.read()

# Сохранение файла
def setFile(path,content,encode="utf-8"):
       with open(path,"w",encoding=encode) as f:
       	return f.write(content)

##########  Работа с изображениями ###########
# Изображение
class Image:
	id,x,y,h,w = 0,0,0,0,0
	x2,y2 = 0,0
	rotated = True
	posed = False
	atlas = 0
	img = None
	path = ""
	preffix = ""
	suffix = ""
	name = ""
	filename = ""
	prop = {'x':0,'y':0,'w':0,'h':0}
	isSetProp = False
	
	def __init__(self,path, preffix = "", suffix = "", id_ = False):
		self.isSetProp = False
		if path and type(path) is str:
			self.path = os.path.realpath(path)
			self.name = os.path.splitext(os.path.split(path)[1])[0]
			self.img = PIMG.open(path)
			if path.split('.')[-1] == "png":
				self.img = self.img.convert('RGBA')
		elif type(path) is list:
			if len(path) == 3:
				self.img = path[0]
			else:
				self.img = PIMG.new(path[0],path[1])
			self.path, self.name = path[-2:]
		self.preffix = preffix
		self.suffix = suffix
		if id_:
			self.id = id_
		else:
			self.id = randint(1,9999999)
		self.w,self.h = self.img.size
		self.posed = False
		self.prop = {'x':0,'y':0,'w':0,'h':0}
		print("Загружаем файл:\n"+self.name+"\nРазмер: "+str(self.w)+"×"+str(self.h)+"\nТип: "+self.img.mode)
		
	def set(self, x, y):
		self.x = x
		self.y = y
		self.x2 = x + self.w
		self.y2 = y + self.h
		
	def rotate(self, degrees = 90, expanding= True):
		self.img = self.img.rotate(degrees,expand=expanding)
		self.w,self.h = self.img.size
	
	def addMargin(self, offset = False, offsets = False):
		pos = [0,0,self.w,self.h]
		if offset:
			pos[0] -= offset
			pos[1] -= offset
			pos[2] += offset
			pos[3] += offset
		elif offsets:
			pos[0] -= offsets[0]
			pos[1] -= offsets[1]
			pos[2] += offsets[2]
			pos[3] += offsets[3]
		self.crop(pos)

	def addCorner(self, radius):
		circle = PIMG.new("L", (radius*2,radius*2), 0)
		draw = ImageDraw.Draw(circle)
		draw.ellipse((0,0,radius*2,radius*2), fill=255)
		alpha = PIMG.new('L', self.img.size, 255)
		w, h = self.img.size
		alpha.paste(circle.crop((0,0,radius,radius)),(0,0))
		alpha.paste(circle.crop((0,radius,radius,radius*2)),(0,h-radius))
		alpha.paste(circle.crop((radius,0,radius*2,radius)),(w - radius,0))
		alpha.paste(circle.crop((radius,radius,radius*2,radius*2)),(w-radius,h-radius))
		self.img.putalpha(alpha)

	def transpose(self):
		if not self.rotated:
			self.rotate(-90)
		else:
			self.rotate(90)
		self.rotated = not self.rotated
	
	def crop(self, left, top = "", right = "", bottom = ""):
		if left== -1 or top== -1 or right== -1 or bottom== -1:
			print("Не верные параметры или изображение пустое")
			return -1
		elif type(left) is int and type(top) is int and type(right) is int and type(bottom) is int:
			self.img = self.img.crop((left,top,right,bottom))
			print("Обрезка x:",left,"y:",top,"w:",right-left,"h:",bottom-top)
		elif (type(left) is list or type(left) is tuple) and len(left) == 4:
			print("Обрезка x:",left[0],"y:",left[1],"w:",left[2]-left[0],"h:",left[3]-left[1])
			self.img = self.img.crop(left)
		else:
			print("Неправильные параметры")
			return -1
		self.w, self.h = self.img.size
		return 1
	
	def getTrimPos(self, image = None, offsets = [0,0,0,0]):
		tmp = np.asarray(self.img if image is None else image)
		top, bottom, left, right = -1,-1,-1,-1
		for i in range(tmp.shape[0]):
			if top == -1:
				if tmp[i].sum() > 0:
					top = i
			if bottom == -1:
				if tmp[tmp.shape[0]-1-i].sum() > 0:
					bottom = tmp.shape[0]-i
			if top >= 0 and bottom >= 0:
				break
		for i in range(tmp.shape[1]):
			if left == -1:
				if tmp[top:bottom,i].sum() > 0:
					left = i
			if right== -1:
				if tmp[top:bottom,tmp.shape[1]-1-i].sum() > 0:
					right = tmp.shape[1]-i
			if left >= 0 and right>= 0:
				break
		left -= offsets[0]
		top -=offsets[1]
		right +=1+offsets[2]
		bottom += 1+offsets[3]
		return (
		0 if left < 0 else tmp.shape[1] if left > tmp.shape[1] else left,
		0 if top < 0 else tmp.shape[0] if top > tmp.shape[0] else top,
		0 if right < 0 else tmp.shape[1] if right > tmp.shape[1] else right,
		0 if bottom < 0 else tmp.shape[0] if bottom > tmp.shape[0] else bottom)
	
	def copy(self):
		return copy.deepcopy(self)
		
	def trim(self):
		self.crop(self.getTrimPos())

	def paste(self, pos, img):
		x, y = pos
		w,h = img.getSize()
		if type(pos[0]) == str:
			if pos[0] == "center":
				x = self.w/2-w/2
			if pos[1] == "center":
				y = self.h/2 - h/2
		x = int(x)
		y = int(y)
		self.img.paste(img.img,(x,y),img.img)
		
	def addBorder(self, size, color, blur = 0):
		y = Image(["RGBA",(int(self.w*size/100), int(self.h*size/100)),self.path,self.name])
		xx = self.copy()
		xx.changeColor(toColor=color, noneThis=1, around=1)
		xx.setScale(size)
		y.paste(("center","center"),xx)
		y.blur(blur)
		y.paste(("center","center"),self)
		y.setSize(height=self.h)
		self.img = y.img
		self.getSize()
		
	def shadow(self, radius, blur, color):
		y = Image(["RGBA",(int(self.w+radius*4), int(self.h+radius*4)),self.path,self.name])
		xx = self.copy()
		xx.changeColor(toColor=color, noneThis=1, around=0)
		xx.resize(int(self.w+radius*2), int(self.h+radius*2))
		y.paste(("center","center"),xx)
		y.blur(blur)
		y.paste(("center","center"),self)
		y.setSize(height=self.h)
		self.img = y.img
		self.getSize()
	
	def targets(self,pos):
		i, j = pos
		yield (i, j)
		yield (i, j-1)
		yield (i, j+1)
		yield (i-1, j)
		yield (i+1, j)
		yield (i-1, j -1)
		yield (i-1, j+1)
		yield (i+1, j-1)
		yield (i+1, j+1)

	def changeColor(self, fromColor = False, toColor = (0,0,0,0), noneThis = False, around = False):
		if not fromColor:
		    fromColor = list(self.img.getpixel((1,1)))
		R,G,B,A = 0,0,0,-1
		R1, G1, B1, A1 = 0,0,0,-1
		R2, G2, B2, A2 = 0,0,0,-1
		b = False
		image = copy.deepcopy(self.img)
		if around or type(fromColor[0]) is list and len(fromColor) == 2:
			self.getSize()
			for x in range(self.w):
				for y in range(self.h):
					R,G,B,A = image.getpixel((x,y))
					if type(fromColor[0]) is list:
						R1,G1,B1,A1 = fromColor[0]
					else:
						R1,G1,B1,A1 = fromColor
					if type(fromColor[0]) is list and len(fromColor) == 2:
						R2,G2,B2,A2 = fromColor[1]
						b = (R1 <= R <= R2 or R1 == -1 and (R2 >= R or R2 == -1) or R2 == -1 and (R1 <= R)) and (G1 <= G <= G2 or G1 == -1 and (G2 >= G or G2 == -1) or G2 == -1 and (G1 <= G)) and (B1 <= B <= B2 or B1 == -1 and (B2 >= B or B2 == -1) or B2 == -1 and (B1 <= B)) and (A1 <= A <= A2 or A1 == -1 and (A2 >= A or A2 == -1) or A2 == -1 and (A1 <= A))
					else:
						b = (R == R1 or R1 == -1) and (G == G1 or G1 == -1) and (B == B1 or B1 == -1) and (A == A1 or A1 == -1)
					if noneThis:
						b = not b
					if b:
						self.setColor((x,y),toColor,around)
		else:
			tmp = self.getArray()
			#tmp.setflags(write=True)
			if noneThis:
				tmp[(tmp != fromColor).all(axis = -1)] = toColor
			else:
				tmp[(tmp == fromColor).all(axis = -1)] = toColor
			self.img = PIMG.fromarray(tmp,'RGBA')
			
	def setColor(self, pos, color, around = False):
		if around:
			for i,j in self.targets(pos):
				self.setColor((i,j),color)
		elif self.w > pos[0] >= 0 and self.h > pos[1] >= 0:
			self.img.putpixel(tuple(pos),tuple(color))
		
	def alpha(self):
		self.changeColor()
	
	def setScale(self, scale):
		self.setSize(width=self.w/100*scale)
		
	def getArray(self):
		return np.array(self.img)

	def resize(self,w,h):
		self.img = self.img.resize((int(w),int(h)), PIMG.ANTIALIAS)
		self.w, self.h = self.img.size
		print("Размер изменён на "+str(self.w)+"×"+str(self.h))
	
	def setSize(self, width = 0, height = 0):
		self.resize(self.w/self.h*height if height else width, self.h/self.w * width if width else height)
	
	def blur(self, iterations = 1):
		for i in range(iterations):
			self.img = self.img.filter(filter=ImageFilter.BLUR)
		
	def setPosFile(self, path):
		if path == self.path:
			nImg = self.img
		else:
			if not os.path.exists(path):
				print("Такого нет...",path)
				return 
			nImg = PIMG.open(path)
			if path.split('.')[-1] == 'png':
				nImg = nImg.convert('RGBA')
		self.isSetProp = True	
		self.prop['x'], self.prop['y'], self.prop['w'], self.prop['h'] = self.getTrimPos(nImg)
		self.prop['w'] -= self.prop['x']
		self.prop['h'] -= self.prop['y']
	
	def addProp(self, key, value):
		self.isSetProp = True
		self.prop[key] = self.name if value == ImageFunc._FILENAME_ else self.id if value == ImageFunc._FILEID_ else value
	
	def setAutoSize(self, min_size = 0, max_size = 0):
		if max_size <= 0:
			max_size = self.h
		if self.prop['h'] > 0:
			self.setSize(height=min([max_size,max([min_size,self.prop['h']*2])]))
		else:
			print("Для этого нужен файл с позицией и размерами картинки")

	def getProp(self, keys = False):
		wtext = ""
		for key in (keys if keys else self.prop.keys()):
			wtext += key+": "+str(self.prop[key])+"\n"
		return wtext[:-1]
		
	def toPyGame(self, pg):
		return pg.image.fromstring(self.img.tobytes(), self.img.size, self.img.mode)
	
	def save(self, path = "", fileName = False,quality=100):
		if path == "":
			path = os.path.split(self.path)[0]
		if '/' in self.filename:
			path = os.path.join(path,os.path.split(self.filename)[0])
		if not os.path.exists(path):
			os.makedirs(path)
		if fileName == False:
			fileName = "0.png"
			while os.path.isfile(os.path.join(path,self.preffix+fileName.split(".")[0]+self.suffix+"."+fileName.split(".")[-1])):
				if fileName.split('.')[0].isdigit():
					fileName = str(int(fileName.split('.')[0])+1)+".png"
				elif '_new' in fileName:
					if '_new.png' in fileName:
						fileName = fileName.replace('.png','0.png')
					else:
						a,b = fileName.split('_new')
						b,c = b.split('.')
						if b.isdigit():
							fileName = a +"_new"+ str(int(b)+1)+"."+c
				else:
					fileName = fileName.replace(".png","_new.png")
		print()
		self.img.save(os.path.join(path,self.preffix+os.path.splitext(fileName)[0]+self.suffix+os.path.splitext(fileName)[1]),os.path.splitext(fileName)[1].upper().strip(". "),quality=quality)
	
	def getSize(self):
		self.w, self.h = self.img.size
		return [self.w,self.h]

# Текстура
class Texture:
	name = ""
	output = {'txtFile':"",'propFile':"",'path':"",'atlas':[]}
	imgList = []
	size = 0
	texture = None
	texturePos = []
	mode = 'RGBA'
	atlasN = 0
	padding = 0
	property = False
	
	def __init__(self, path, atlasName = "Texture", size = 2048, mode = 'RGBA', prop = False, padding = 0):
		self.output = {'txtFile':"",'propFile':"",'path':"",'atlas':[]}
		self.size = size
		self.output['path'] = path	
		self.name = atlasName
		self.mode = mode
		self.property = prop
		self.imgList = []
		self.atlasN = 0
		self.padding = padding
		self.texture = None
		self.texturePos = []
	
	def new(self):
		if not self.texture is None:
			self.output['atlas'].append(self.texture)
		self.texture = PIMG.new(self.mode,[self.size]*2)
	
	def update(self):
		if len(self.output['atlas']) <= self.atlasN:
			self.output['atlas'].append(self.texture)
	
	def delete(self):
		self.output['atlas'] = []
	
	def draw(self, v = False, crop = False):
		text = ""
		ptxt = ""
		for j in range(self.atlasN+1):
			self.new()
			wtext = "{TOP}\n"
			for i in self.imgList:
				i.getSize()
				if i.posed and i.atlas == j:
					if self.property:
						ptxt += i.getProp()+"\n"*2 if i.isSetProp else ""
					if v:
						i.transpose()
						self.texture.paste(i.img,(i.x,i.y))
						wtext += i.preffix + i.name + i.suffix + "\n rotate: "+str(not i.rotated).lower() + "\n xy: " + str(i.x) + ", " + str(i.y) + "\n size: "+str(i.w if i.rotated else i.h) +", "+str(i.h if i.rotated else i.w) + "\n orig: "+str(i.w if i.rotated else i.h) +", "+str(i.h if i.rotated else i.w) + "\n offset: 0,0\n index: -1\n"
						i.transpose()
					else:
						self.texture.paste(i.img,(i.y,i.x))
						wtext += i.preffix + i.name + i.suffix + "\n rotate: "+str(not i.rotated).lower() + "\n xy: " + str(i.y) + ", " + str(i.x) + "\n size: "+str(i.w if i.rotated else i.h) +", "+str(i.h if i.rotated else i.w) + "\n orig: "+str(i.w if i.rotated else i.h) +", "+str(i.h if i.rotated else i.w) + "\n offset: 0,0\n index: -1\n"
			if crop:
				self.texture = self.texture.crop(self.imgList[0].getTrimPos(self.texture))
			text += wtext.replace("{TOP}",("" if j == 0 else "\n")+self.name+"-"+str(j)+".png\nsize: "+str(self.texture.width)+", "+str(self.texture.height) + "\nformat: RGBA8888\nfilter: Linear,Linear\nrepeat: none")
		self.update()
		self.output['txtFile'] = text
		self.output['propFile'] = ptxt
		
	def toPyGame(self, pg):
		ls = []
		for i in self.output['atlas']:
			ls.append(pg.image.fromstring(i.tobytes(), i.size, i.mode))
		return ls
		
	def save(self):
		print("Сохранение...")
		FileManager.mkDirs(None,self.output["path"])
		for i in range(len(self.output['atlas'])):
			self.output['atlas'][i].save(os.path.join(self.output['path'],self.name+"-"+str(i)+".png"),'PNG',quality=100)
			print(self.name+"-"+str(i)+".png")
		if self.property:
			setFile(os.path.join(self.output['path'],self.name+".prop"),self.output['propFile'])
			print(self.name+".prop")
		setFile(os.path.join(self.output['path'],self.name+".pref"),self.output['txtFile'].strip())
		print(self.name+".pref")
		print()
			
	def getProp(self):
		return self.output['propFile']
		
	def getPositions(self,pics = False,atlasN = 0):
		queue = []
		posed = []
		for i in (pics if pics else self.imgList):
			if 0 < i.w <= self.size and 0 < i.h <= self.size:
				i.w += self.padding
				i.h += self.padding
				queue.append(i)
		queue=sorted(queue,key=lambda x:x.w)[::-1]
		alll = len(queue)
		print("\nПоиск оптимальных положений картинок:")
		while alll > len(posed):
			if len(self.texturePos) > atlasN:
				a = self.texturePos[atlasN]
			else:
				a = np.zeros((self.size,self.size),dtype=np.int8)
			i = 0
			while i < self.size:
				j = 0
				while j < self.size:
					dqueue = []
					if alll == len(posed):
						i = self.size
						j = self.size
						break
					if a[i,j] == 0:
						for q in queue[::-1]:
							if i+q.h <= self.size and j + q.w <= self.size and (a[i:i+q.h,j:j+q.w] == 0).all():
								q.set(i,j)
								dqueue.append(q)
							elif i+q.h <= self.size and j + q.w <= self.size and (a[i:i+q.w,j:j+q.h] == 0).all():
								q.set(i,j)
								q.transpose()
								dqueue.append(q)
							else:
								break
						if len(dqueue) > 0:
							a[i:i+dqueue[-1].h,j:j+dqueue[-1].w] = 1
							self.setPosed(dqueue[-1].id,atlasN)
							posed.append(dqueue[-1])
							print("\rПрогресс:",round(100/alll*len(posed),2),"%"+"    ",end="")
							queue.remove(dqueue[-1])
							j += dqueue[-1].w-1
					b = np.where(a[i,j+1:]== 0)
					if len(b[0]) > 0:
						j += b[0][0]+1
					else:
						j = self.size
				b = np.where(a[i+1:] == 0)
				if len(b[0]) > 0:
					i += b[0][0]+1
				else:
					i = self.size
				while i < self.size and (a[i] == a[i-1]).all():
					i+=1
			if len(self.texturePos) <= atlasN:
				self.texturePos.append(a)
			atlasN+=1
		self.atlasN = max([atlasN-1,self.atlasN])
		
	def setImages(self, img):
		self.imgList = img
	
	def delImages(self):
		self.imgList = []
	
	def addImage(self, img):
		if type(img) is list:
			self.imgList.extend(img)
		else:
			self.imgList.append(img)
	
	def setPosed(self,id,atlas):
		for i in self.imgList:
			if i.id == id:
				i.posed = True
				i.atlas = atlas
				break

# Готовая текстура
class sTexture:
	txt = ""
	_map_ = {}
	Images = []
	Atlases = {}
	path = ""
	def __init__(self, path):
		self.path = os.path.split(path)[0]
		self.txt = getFile(path).strip("\n\t\r")
		self.textToMap(self.txt)
		for i in self.getAtlasNames():
			self.Atlases[i] = PIMG.open(os.path.join(self.path,i)).convert("RGBA")
		self.getImages()
	
	def getImages(self):
		self.Images = []
		for i in self.getAtlasNames():
			for j in self._map_[i]["images"]:
				x,y = map(lambda x: int(x.strip()),j["xy"].split(","))
				w,h = map(lambda x: int(x.strip()),j["size"].split(","))
				r = not j["rotate"] == "true"
				img = Image([self.Atlases[i].crop((x,y,x+w if r else x + h,y+h if r else y + w)),
				self.path,j["name"]])
				if not r:
					img.rotated = False
					img.transpose()
				self.Images.append([j["name"]+".png",img])
				print()
	
	def saveResized(self, sizes, path = False):
		if not path:
			path = os.path.join(self.path,"MIP_TEXTURES")
		FileManager.mkDirs(None,path)
		i = list(self.getAtlasNames())[0]
		sizes = sorted(sizes)[::-1]
		for j in range(len(sizes)):
			size = sizes[j]
			ns = size/(self.Atlases[i].size[0] if j == 0 else sizes[j-1])
			print("Изменение картинок под новые размеры")
			for nn in self.Images:
				nn[1].setSize(height=int(nn[1].h*ns))
			createTexture(path=path,size=size,atlasName=os.path.splitext(i)[0]+"["+str(size)+"]",img=list(map(lambda x: x[1],self.Images)))
	
	def saveImages(self, path = False, imgNames = False):
		if not path:
			path = os.path.join(self.path,"ATLAS_IMG")
		FileManager.mkDirs(None,path)
		for i in self.Images:
			if imgNames:
				if i[0] in imgNames:
					print("Сохранение...",i[0])
					i[1].save(path=path,fileName=i[0])
			else:
				print("Сохранение...",i[0])
				i[1].save(path=path,fileName=i[0])
		
	def getAtlasNames(self):
		return self._map_.keys()
		
	def textToMap(self, text):
		files = text.split("\n\n")
		txt = {}
		for file in files:
			lines = file.split("\n")
			txt[lines[0]] = {}
			for i in range(1,5):
				line = list(map(lambda x: x.strip(),lines[i].split(":")))
				txt[lines[0]][line[0]] = line[1]
			txt[lines[0]]["images"] = []
			for i in range(5,len(lines)):
				if (i+2)% 7 == 0:
					txt[lines[0]]["images"].append({})
				line = list(map(lambda x: x.strip(),lines[i].split(":"))) if ":" in lines[i] else ["name",lines[i]]
				txt[lines[0]]["images"][-1][line[0]] = line[1]
		self._map_ = txt
		return txt
	
	def MapToText(self, mapp):
		txt3 = ""
		for i,k in mapp.items():
			txt3 += i +"\n"
			for j,n in k.items():
				if j != "images":
					txt3 += j+": "+n + "\n"
			for m in k["images"]:
				for j, n in m.items():
					if "name" == j:
						txt3 += n + "\n"
					else:
						txt3 += " "+j + ": "+n + "\n"
			txt3 += '\n'
		txt3 = txt3.strip()
		self.txt = txt3
		return txt3

# Получить изображения по пути
def getImages(path):
    ii = []
    for file in sorted(os.listdir(path)):
    	if os.path.isdir(os.path.join(path,file)):
    		continue
    	im = Image(os.path.join(path,file),id_=len(ii)+1)
    	ii.append(im) 
    return ii

# Свойства
class Property:
	# Параметры
	fText = ""
	mArr = []
	# Инициализация
	def __init__(self, text=""):
		self.fText = text
	# Загрузить новый файл свойств
	def load(self, path = False, text = False):
		if text:
			self.fText = text
		elif path:
			with open(path,'r') as f:
				self.fText = f.read()
		n = []
		for i in self.fText.strip('\n\r\t ').split("\n\n"):
			m = {}
			for j in i.strip('\n\r\t ').split('\n'):
				k = j.strip('\n\r\t ').split(':')
				m[k[0].strip('\n\r\t ')] = k[1].strip('\n\r\t ')
			n.append(m)
		self.mArr = n
	# Сортировка
	def sort(self, keys = False):
		txt = ""
		for i in sorted(self.mArr,key=lambda x: int(x['id'])):
			for j in (keys if keys else sorted(i.keys())):
				if j not in i.keys():
					continue
				txt += str(j) + ": " + str(i[j])+"\n"
			txt += "\n"
		self.fText = txt.strip('\t\n\r ')
	# Добавить свойство
	def setProp(self, id, key, value):
		for i in sorted(self.mArr,key=lambda x: int(x['id'])):
			if str(i['id'])== str(id):
				i[key] = value
				break
		self.sort()
	# Сохранить
	def save(self, savepath):
		with open(savepath, "w") as f:
			f.write(self.fText)

# Пакетная обработка
# path - путь к изображениям (str)
# savepath - путь сохранения картинок (str или bool)
# arg - список функций которые будут применяться, в качестве параметра передается изображение (list)
# types - список расширений картинок (list)
# r - рекурсивный проход по всем вложенным папкам (bool)
# imgs - не нужно указывать
# otherPaths - создавать пути full,croped,resize,resulu (bool)
# mainpath - не указывать
# ids - начало id (int)
def batchEditor(path, savepath = False, arg = [], types=False, r=False, imgs = [], otherPaths = False, mainpath = False, ids = 0):
	if not mainpath:
		mainpath = path
	fm = FileManager(path = path, filters = types,otherPaths = otherPaths)
	for i in sortingDir(fm.getFiles(fm.PATH)):
		ids+=1
		#clear()
		x = Image(os.path.join(fm.getPath(fm.PATH),i),id_=ids)
		x.filename = x.path[len(os.path.realpath(mainpath))+1:]
		for j in arg:
			j(x)
		if savepath:
			x.save(path=savepath,fileName=i)
		imgs.append([x.filename,x])
		print()
	if r:
		for i in sortingDir(fm.getDirs(fm.PATH)):
			batchEditor(os.path.join(path,i),savepath, arg,types, r, imgs, otherPaths,mainpath,ids)
	return imgs
	
# Обрезка картинок по самой большой из них
def MaxTrimming(path, arg = [], save = False, r = False):
	maxTrim = []
	arg.append(ImageFunc.GETTRIM(maxTrim))
	img = batchEditor(path, arg=arg, r = r)
	maxTrim = [
		sorted(maxTrim,key=lambda x: x[0])[0][0],
		sorted(maxTrim,key=lambda x: x[1])[0][1],
		sorted(maxTrim,key=lambda x: x[2])[-1][2],
		sorted(maxTrim,key=lambda x: x[3])[-1][3]
	]
	cImgs = categorize(img)
	for i in cImgs.keys():
		for img in cImgs[i]:
			img.crop(maxTrim)
			if save:
				img.save(path=os.path.join(path,"result",i),fileName=img.filename)
	return cImgs

# Разбиение на категории
def categorize(x):
	y = {}
	for i in x:
		z = os.path.split(i[0])[0]
		if z not in y.keys():
			y[z] = []
		y[z].append(i[1])
	return y

# Создание текстуры
# createTexture(path, prop, size, vertical, img, atlasName, crop)
# path - путь для сохранения (str)
# prop - сохранить параметры (list или bool) 
# size - максимальный размер атласа (int)
# vertical - заполнять в вертикальном порядке (bool)
# img - изображения (list или bool)
# atlasName - имя атласа (str)
# crop - обрезать атласы (bool)
def createTexture(path, prop = False, size = 2048, vertical = False, img = False, atlasName = "graphic", crop = False, padding = 0):
	T = Texture(path=path,atlasName=atlasName,size=size,mode='RGBA',prop=prop, padding=padding)
	T.addImage(img if img else getImages(path))
	T.getPositions()
	T.draw(v=vertical,crop=crop)
	if prop:
		P = Property(T.getProp())
		P.load()
		P.sort(prop)
		T.output['propFile'] = P.fText
	T.save()

# Вытащить изображения из текстуры
def getImagesFromTexture(path, save=False):
	A = sTexture(path)
	A.saveImages(path=save)

# Изменение размера текстуры
def resizeTexture(path, sizes, save=False):
	A = sTexture(path)
	A.saveResized(sizes,save)

# Функции для пакетной обработки изображений
class ImageFunc:
	# Переменныена указатели
	# Имя файла
	_FILENAME_ = "__filename__"
	# Путь
	_FILEID_ = "__id__"
	
	# Функции изображений
	# Добавление тени
	# ADDPROP(key, value)
	SHADOW = lambda x, y, z: lambda g: g.shadow(x,y,z)

	# Изменение размера изображений с сохранением пропорций
	# RESIZE(height, width)
	RESIZE = lambda x,y: lambda g: g.setSize(height=x,width=y)
	
	# Авторазмер (нужен SETPROP который ниже)
	# ARESIZE(min,max)
	ARESIZE = lambda x,y: lambda g: g.setAutoSize(min_size=x,max_size=y)
	
	# Обрезка
	# TRIM
	TRIM = lambda x: x.trim()
	
	# Обрезка с отступами
	# TRIM_WO([ left, top, right, bottom ])
	TRIM_WO = lambda g: lambda x: x.crop(x.getTrimPos(offsets=g))
	
	# Добавление одинаковых отступов
	# ADD_MARGIN(offset)
	ADD_MARGIN = lambda g: lambda x: x.addMargin(offset=g)
	
	# Добавление разных отступов
	# ADD_MARGINS([ left, top, right, bottom ])
	ADD_MARGINS = lambda g: lambda x: x.addMargin(offsets=g)
	
	# Обрезка изображений
	# CROP([ left, top, right, bottom ])
	CROP = lambda x: lambda g: g.crop(x)
	
	# Установка позиций картинок на фоне
	# SETPOS(path)
	# path - путь к папке, где хранятся картинки с оригинальной позицией
	SETPOS = lambda x: lambda g: g.setPosFile(path=os.path.join(x,g.filename))
	
	# Добавление или изменение параметров
	# ADDPROP(key, value)
	ADDPROP = lambda y, z: lambda g: g.addProp(key=y,value=z)
	
	# Получение параметров
	# GETPROP(list)
	GETPROP = lambda g: lambda x: g.append(x.getProp())
	
	# Получения изображений для pygame
	# GETPGIMAGE(pygame, list)
	GETPGIMAGE = lambda pg, ls: lambda x: ls.append(x.toPyGame(pg))
	
	# Получение размеров изображений
	# GETSIZE(list)
	GETSIZE = lambda g: lambda x: g.append(x.getSize())
	
	# Получение имен изображений
	# GETNAME(list)
	GETNAME = lambda g: lambda x: g.append(x.name)
	
	# Получение позиций для обрезки
	# GETTRIM(list)
	GETTRIM = lambda g: lambda x: g.append(x.getTrimPos())

##############  Работа с видео  ###############
# Видео
class Video:
	filename = 'video.mp4'
	frames_per_second = 24.0
	res = '1080p'
	STD_DIMENSIONS =  {
		"480p": (640, 480),
    	"720p": (1280, 720),
    	"1080p": (1920, 1080),
  	  "4k": (3840, 2160),
	}
	VIDEO_TYPE = {
    	#'avi': cv2.VideoWriter_fourcc(*'XVID'),
    	#'mp4': cv2.VideoWriter_fourcc(*'H264'),
    	#'mp4': cv2.VideoWriter_fourcc(*'XVID'),
    }
	
	def change_res(self, cap, width, height):
	   cap.set(3, width)
	   cap.set(4, height)
	
	def get_dims(self, cap, res='1080p'):
	   width, height = self.STD_DIMENSIONS["480p"]
	   if res in self.STD_DIMENSIONS:
	   	width,height = self.STD_DIMENSIONS[res]
	   self.change_res(cap, width, height)
	   return width, height
	   
	def get_video_type(self, filename):
	   filename, ext = os.path.splitext(filename)
	   if ext in self.VIDEO_TYPE:
	   	return  self.VIDEO_TYPE[ext]
	   return self.VIDEO_TYPE['avi']
	   
	def start(self):
		st = time.time()
		cap = cv2.VideoCapture(0)
		out = cv2.VideoWriter(self.filename,
		self.get_video_type(self.filename), self.frames_per_second, self.get_dims(cap, self.res))
		while True:
		  ret, frame = cap.read()
		  out.write(frame)
		  #cv2.imshow('frame',frame)
		  #if cv2.waitKey(1) & 0xFF == ord('q'):
		  #	break
		  if time.time() - st > 3:
		  	break
		cap.release()
		out.release()
		#cv2.destroyAllWindows()

# Разбиение видео на кадры
def getFrame(video = 0):
    import cv2
    vidcap = cv2.VideoCapture(video)
    outpath = video.split('.')[0] if video != 0 else "vid"
    count = 0
    if os.path.isdir(outpath):
        shutil.rmtree(outpath)
    os.mkdir(outpath)
    while vidcap.isOpened():
        success, image = vidcap.read()
        if success:
            print("\rКол-во кадров: ",count,end="")
            PIMG.fromarray(image).save(os.path.join(outpath, '%d.png') % count)
            count += 1
        else:
            break
    #cv2.destroyAllWindows()
    vidcap.release()

############### Доп функции  ################
# Очистка вывода
def clear():
	os.system('cls' if os.name == 'nt' else 'clear')