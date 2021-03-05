import pygame as pg
map = []
with open("map.txt","r") as f:
    for i in f.read().split("\n"):
        map.append(list(i))
MAP_SIZE = [len(map[0]),len(map)]
BLOCK_SIZE = int(1500 / MAP_SIZE[0])
HEIGHT = BLOCK_SIZE * MAP_SIZE[1]
WOFFSET = 20
WIDTH = BLOCK_SIZE * (MAP_SIZE[0]+WOFFSET)
FPS = 30 
pg.init()
pg.mixer.init()  # для звука
screen = pg.display.set_mode((WIDTH, HEIGHT))
pg.display.set_caption("My Game")
clock = pg.time.Clock()
blue = (50,80,255)
black = (0,0,0)
running = True

block_map = {}
font = pg.font.SysFont("Google Sans",20)

def add(name, color, rects, value, color2 = False):
    global block_map
    block_map[value] = [pg.Surface((BLOCK_SIZE,BLOCK_SIZE)),font.render(name,1,black),name]
    pg.draw.rect(block_map[value][0],(255,255,255),(0,0,BLOCK_SIZE,BLOCK_SIZE))
    col = False
    if not color2:
        color2 = color
    for i in rects:
        pg.draw.rect(block_map[value][0],color2 if col else color,i)
        col = True

add("Пустота",blue,[],'0')
add("OUT HORIZONT",blue,[[0,BLOCK_SIZE/4,BLOCK_SIZE,BLOCK_SIZE/2]],'h')
add("OUT VERTICAL",blue,[[BLOCK_SIZE/4,0,BLOCK_SIZE/2,BLOCK_SIZE]],'v')
add("OUT DOWN RIGHT",blue,[[0,BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2],[BLOCK_SIZE/4,0,BLOCK_SIZE/2,BLOCK_SIZE/2]],'a')
add("OUT DOWN LEFT",blue,[[BLOCK_SIZE/2,BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2],[BLOCK_SIZE/4,0,BLOCK_SIZE/2,BLOCK_SIZE/2]],'b')
add("OUT TOP RIGHT",blue,[[0,BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2],[BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2,BLOCK_SIZE/2]],'c')
add("OUT TOP LEFT",blue,[[BLOCK_SIZE/2,BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2],[BLOCK_SIZE/4,BLOCK_SIZE/2,BLOCK_SIZE/2,BLOCK_SIZE/2]],'d')
add("IN DOWN RIGHT",blue,[[0,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3],[BLOCK_SIZE/3,0,BLOCK_SIZE/3,BLOCK_SIZE/2]],'z')
add("IN DOWN LEFT",blue,[[BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3],[BLOCK_SIZE/3,0,BLOCK_SIZE/3,BLOCK_SIZE/2]],'x')
add("IN TOP RIGHT",blue,[[0,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3],[BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2]],'w')
add("IN TOP LEFT",blue,[[BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3],[BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2]],'p')
add("OUT HORIZONT",blue,[[0,BLOCK_SIZE/3,BLOCK_SIZE,BLOCK_SIZE/3]],'f')
add("OUT VERTICAL",blue,[[BLOCK_SIZE/3,0,BLOCK_SIZE/3,BLOCK_SIZE]],'e')
add("BASE LINE",black,[[0,BLOCK_SIZE/3,BLOCK_SIZE,BLOCK_SIZE/3]],'l')
add("OUT VERTICAL LEFT",blue,[[BLOCK_SIZE/4,0,BLOCK_SIZE/2,BLOCK_SIZE],[0,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3]],'y')
add("OUT VERTICAL RIGHT",blue,[[BLOCK_SIZE/4,0,BLOCK_SIZE/2,BLOCK_SIZE],[BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3]],'q')
add("OUT HORIZONT TOP",blue,[[0,BLOCK_SIZE/4,BLOCK_SIZE,BLOCK_SIZE/2],[BLOCK_SIZE/3,0,BLOCK_SIZE/3,BLOCK_SIZE/2]],'u')
add("OUT HORIZONT DOWN",blue,[[0,BLOCK_SIZE/4,BLOCK_SIZE,BLOCK_SIZE/2],[BLOCK_SIZE/3,BLOCK_SIZE/2,BLOCK_SIZE/3,BLOCK_SIZE/2]],'t')
add("PACMAN",(255,255,0),[[0,0,BLOCK_SIZE,BLOCK_SIZE],[BLOCK_SIZE-6,6,3,3],[BLOCK_SIZE/2,BLOCK_SIZE/2,BLOCK_SIZE/2,BLOCK_SIZE/3]],'9',black)
add("GHOST 5",(255,0,0),[[0,0,BLOCK_SIZE,BLOCK_SIZE]],'5')
add("GHOST 6",(255,0,255),[[0,0,BLOCK_SIZE,BLOCK_SIZE]],'6')
add("GHOST 7",(255,255,0),[[0,0,BLOCK_SIZE,BLOCK_SIZE]],'7')
add("GHOST 8",(0,255,255),[[0,0,BLOCK_SIZE,BLOCK_SIZE]],'8')
add("POINT",black,[[BLOCK_SIZE/2-5,BLOCK_SIZE/2-5,10,10]],'2')
add("BIG POINT",black,[[BLOCK_SIZE/3,BLOCK_SIZE/3,BLOCK_SIZE/3,BLOCK_SIZE/3]],'4')
add("EATED",(230,230,230),[[0,0,BLOCK_SIZE,BLOCK_SIZE]],'3')

keys = [x for x in block_map.keys()]
selected = keys[0]
while running:
    screen.fill((255,255,255))
    for event in pg.event.get():
        # check for closing window
        if event.type == pg.QUIT:
            running = False
        elif event.type == pg.KEYDOWN:
            if event.key == pg.K_s:
                with open("map.txt","w") as f:
                    f.write("\n".join("".join(i) for i in map))
    if (pg.mouse.get_pressed()[0]):
        c = pg.mouse.get_pos()
        c = [c[0]//BLOCK_SIZE,c[1]//BLOCK_SIZE]
        if MAP_SIZE[0] > c[0] >= 0 and MAP_SIZE[1] > c[1] >= 0:
            map[c[1]][c[0]] = selected
        else:
            c[1] = pg.mouse.get_pos()[1]//(5+BLOCK_SIZE)
            if 0 < c[1] <= len(keys):
                selected = keys[c[1]-1]
    for x in range(MAP_SIZE[1]):
        for y in range(MAP_SIZE[0]):
            screen.blit(block_map[map[x][y]][0],(y*BLOCK_SIZE,x*BLOCK_SIZE))
    for i in range(len(block_map)):
        screen.blit(block_map[keys[i]][0],(WIDTH-BLOCK_SIZE*(WOFFSET-1),BLOCK_SIZE+(5+BLOCK_SIZE)*i))
        screen.blit(block_map[keys[i]][1],(WIDTH-BLOCK_SIZE*(WOFFSET-3),BLOCK_SIZE+10+(5+BLOCK_SIZE)*i))
    pg.draw.rect(screen,(230,230,230),(WIDTH-BLOCK_SIZE*WOFFSET,HEIGHT-BLOCK_SIZE,BLOCK_SIZE*WOFFSET,BLOCK_SIZE))
    screen.blit(block_map[selected][0],(WIDTH-BLOCK_SIZE*(WOFFSET-1),HEIGHT-BLOCK_SIZE))
    screen.blit(block_map[selected][1],(WIDTH-BLOCK_SIZE*(WOFFSET-3),HEIGHT-BLOCK_SIZE))
    pg.display.update()
    clock.tick(FPS)
    # Ввод процесса (события)
    # Обновление
    # Визуализация (сборка)