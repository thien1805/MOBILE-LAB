import pygame
import neat
import os
import pickle

# --- CẤU HÌNH CƠ BẢN ---
WIN_WIDTH = 500
WIN_HEIGHT = 800
BIRD_SIZE = 50
PIPE_WIDTH = 80
GAP_SIZE = 200

pygame.font.init()
STAT_FONT = pygame.font.SysFont("comicsans", 30)

def get_circular_bird(img_path, size):
    try:
        raw_img = pygame.image.load(img_path).convert_alpha()
        scaled_img = pygame.transform.scale(raw_img, (size, size))
        circle_surface = pygame.Surface((size, size), pygame.SRCALPHA)
        pygame.draw.circle(circle_surface, (255, 255, 255, 255), (size // 2, size // 2), size // 2)
        circle_surface.blit(scaled_img, (0, 0), special_flags=pygame.BLEND_RGBA_MIN)
        return circle_surface
    except Exception:
        fallback = pygame.Surface((size, size), pygame.SRCALPHA)
        pygame.draw.circle(fallback, (255, 0, 0), (size // 2, size // 2), size // 2)
        return fallback

BIRD_IMG = None
PIPE_IMG_BASE = None
BG_IMG = None

class Bird:
    MAX_ROTATION = 25
    ROT_VEL = 20
    ANIMATION_TIME = 5

    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.tilt = 0
        self.tick_count = 0
        self.vel = 0
        self.height = self.y
        self.img = BIRD_IMG

    def jump(self):
        self.vel = -10.5
        self.tick_count = 0
        self.height = self.y

    def move(self):
        self.tick_count += 1
        d = self.vel * self.tick_count + 1.5 * self.tick_count**2
        if d >= 16: d = 16
        if d < 0: d -= 2
        self.y = self.y + d

        if d < 0 or self.y < self.height + 50:
            if self.tilt < self.MAX_ROTATION:
                self.tilt = self.MAX_ROTATION
        else:
            if self.tilt > -90:
                self.tilt -= self.ROT_VEL

    def draw(self, win):
        rotated_image = pygame.transform.rotate(self.img, self.tilt)
        new_rect = rotated_image.get_rect(center=self.img.get_rect(topleft=(self.x, self.y)).center)
        win.blit(rotated_image, new_rect.topleft)

    def get_mask(self):
        return pygame.mask.from_surface(self.img)

class Pipe:
    def __init__(self, x):
        self.x = x
        self.top_height = 0
        self.gap = GAP_SIZE
        self.passed = False
        import random
        self.set_height()

    def set_height(self):
        import random
        self.top_height = random.randrange(50, 450)

    def move(self):
        self.x -= 5

    def draw(self, win):
        top_flip = pygame.transform.flip(PIPE_IMG_BASE, False, True)
        top_stretched = pygame.transform.scale(top_flip, (PIPE_WIDTH, self.top_height))
        win.blit(top_stretched, (self.x, 0))

        bottom_y = self.top_height + self.gap
        bottom_height = WIN_HEIGHT - bottom_y
        bottom_stretched = pygame.transform.scale(PIPE_IMG_BASE, (PIPE_WIDTH, bottom_height))
        win.blit(bottom_stretched, (self.x, bottom_y))

    def collide(self, bird):
        bird_mask = bird.get_mask()
        top_flip = pygame.transform.flip(PIPE_IMG_BASE, False, True)
        top_stretched = pygame.transform.scale(top_flip, (PIPE_WIDTH, self.top_height))
        top_mask = pygame.mask.from_surface(top_stretched)
        bottom_y = self.top_height + self.gap
        bottom_stretched = pygame.transform.scale(PIPE_IMG_BASE, (PIPE_WIDTH, WIN_HEIGHT - bottom_y))
        bottom_mask = pygame.mask.from_surface(bottom_stretched)
        top_offset = (self.x - bird.x, 0 - round(bird.y))
        bottom_offset = (self.x - bird.x, bottom_y - round(bird.y))
        return bird_mask.overlap(top_mask, top_offset) or bird_mask.overlap(bottom_mask, bottom_offset)

def draw_window(win, bird, pipes, score):
    win.blit(BG_IMG, (0,0))
    for pipe in pipes:
        pipe.draw(win)

    score_label = STAT_FONT.render(f"Score: {score}", 1, (255,255,255))
    win.blit(score_label, (WIN_WIDTH - score_label.get_width() - 15, 10))

    # Đổi text để biết là đang chạy file AI đã trained
    gen_label = STAT_FONT.render("AI: PRO MODE", 1, (255,255,255))
    win.blit(gen_label, (10, 10))

    bird.draw(win)
    pygame.display.update()

def run_best_bird(config_path, genome_path):
    # 1. Tải cấu hình mạng
    config = neat.config.Config(neat.DefaultGenome, neat.DefaultReproduction,
                         neat.DefaultSpeciesSet, neat.DefaultStagnation,
                         config_path)

    # 2. Tải bộ não đã lưu
    with open(genome_path, "rb") as f:
        genome = pickle.load(f)

    # 3. Nạp bộ não vào mạng nơ-ron
    net = neat.nn.FeedForwardNetwork.create(genome, config)
    bird = Bird(230, 350)
    
    pipes = [Pipe(600)]
    win = pygame.display.set_mode((WIN_WIDTH, WIN_HEIGHT))
    pygame.display.set_caption("Flappy Nhan - Trained AI")
    clock = pygame.time.Clock()
    score = 0
    run = True

    while run:
        clock.tick(30) # Chạy ở tốc độ 30 FPS bình thường
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                run = False
                pygame.quit()
                quit()

        bird.move()

        pipe_ind = 0
        if len(pipes) > 1 and bird.x > pipes[0].x + PIPE_WIDTH:
            pipe_ind = 1

        # Cung cấp tọa độ để não xử lý và ra quyết định
        output = net.activate((bird.y,
                               abs(bird.y - pipes[pipe_ind].top_height),
                               abs(bird.y - (pipes[pipe_ind].top_height + pipes[pipe_ind].gap))))

        if output[0] > 0.5:
            bird.jump()

        add_pipe = False
        rem = []
        for pipe in pipes:
            if pipe.collide(bird):
                print(f"Bùm! Trò chơi kết thúc với điểm số: {score}")
                run = False

            if not pipe.passed and bird.x > pipe.x:
                pipe.passed = True
                add_pipe = True

            pipe.move()
            if pipe.x + PIPE_WIDTH < 0:
                rem.append(pipe)

        if add_pipe:
            score += 1
            pipes.append(Pipe(600))

        for r in rem:
            pipes.remove(r)

        if bird.y + bird.img.get_height() >= WIN_HEIGHT or bird.y < 0:
            print(f"Bùm! Trò chơi kết thúc với điểm số: {score}")
            run = False

        draw_window(win, bird, pipes, score)

if __name__ == "__main__":
    try:
        BIRD_IMG = get_circular_bird("f1.jpg", BIRD_SIZE)
        PIPE_IMG_BASE = pygame.image.load("pipe.webp").convert_alpha()
        BG_IMG = pygame.transform.scale(pygame.image.load("background.png"), (WIN_WIDTH, WIN_HEIGHT)).convert()
    except Exception as e:
        print("Lỗi tải ảnh:", e)
        BIRD_IMG = get_circular_bird("", BIRD_SIZE)
        PIPE_IMG_BASE = pygame.Surface((PIPE_WIDTH, 500))
        PIPE_IMG_BASE.fill((0, 255, 0))
        BG_IMG = pygame.Surface((WIN_WIDTH, WIN_HEIGHT))
        BG_IMG.fill((0, 0, 255))

    local_dir = os.path.dirname(__file__)
    config_path = os.path.join(local_dir, "config-feedforward.txt")
    genome_path = os.path.join(local_dir, "best_bird.pkl")
    
    if not os.path.exists(genome_path):
        print("Không tìm thấy file best_bird.pkl! Hãy chạy lại file main.py để huấn luyện AI trước.")
    else:
        run_best_bird(config_path, genome_path)