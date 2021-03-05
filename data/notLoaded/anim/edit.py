from GameTools import *

def _format_(fn : str, height = 246, margin = 5) -> Image:
    """ Подготовка изображений для добавления в атлас
        ---------------------------------------------
        
        Параметры:
        ----------
        fn : str
            путь до файла
        height : int
            необходимая высота картинки
        margin : int
            отступы по краям
    """
    x = Image(fn)
    x.trim()
    x.setSize(height=height)
    x.addMargin(margin)
    x.save(path="cache",fileName=os.path.split(fn)[-1])
    return x

def createAtlas(_path_,_other_):
    # Загрузка изображений
    _images_paths_ = os.listdir(_path_)
    
    # Подготовка
    _images_ = []
    for i in _images_paths_:
        if i in _other_:
            _images_.append(_format_(os.path.join(_path_,i),48,5))
        else:
            _images_.append(_format_(os.path.join(_path_,i)))

    # Создание атласа
    createTexture(os.path.join(_path_,os.pardir,"textures"),size=2048,img=_images_,atlasName="graphic",padding=5)

createAtlas("C:/Users/Abdulla/Desktop/GameData/notLoaded/anim/images",["items_panel_arrow.png"])