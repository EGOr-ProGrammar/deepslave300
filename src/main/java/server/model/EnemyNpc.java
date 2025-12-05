package server.model;

import shared.Position;

import java.util.UUID;

/**
 * Представляет одного враждебного NPC (моба) в игровом мире.
 * Хранит позицию, статы (HP, атака), радиус агрессии и состояние жизни.
 */
public class EnemyNpc {
    private final UUID id;
    private Position position;
    private int hp;
    private final int maxHp;
    private final int attack;
    private final int aggroRange;
    private boolean alive;

    // Координаты уровня и карты, где находится моб
    private int currentLevel;
    private int currentMapGridX;
    private int currentMapGridY;

    // Символ для отображения на клиенте
    private final char symbol;

    /**
     * Конструктор моба с базовыми параметрами.
     *
     * @param position начальная позиция
     * @param maxHp максимальное здоровье
     * @param attack урон моба
     * @param aggroRange радиус агрессии (в клетках)
     * @param symbol символ для отображения (например, 'M', 'G', 'D')
     */
    public EnemyNpc(Position position, int maxHp, int attack, int aggroRange, char symbol) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.aggroRange = aggroRange;
        this.symbol = symbol;
        this.alive = true;
        this.currentLevel = 1;
        this.currentMapGridX = 0;
        this.currentMapGridY = 0;
    }
    public static EnemyNpc createBasicMob(Position position) {
        return new EnemyNpc(position, 10, 3, 5, 'M');
    }

    /**
     * Нанести урон мобу. Если HP <= 0, моб считается мертвым.
     * @param damage количество урона
     */
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
        if (this.hp <= 0) {
            this.alive = false;
        }
    }

    /**
     * Вычисляет расстояние до заданной позиции.
     * Используется для проверки агро-радиуса.
     */
    public int distanceTo(Position target) {
        return Math.abs(this.position.x() - target.x()) +
                Math.abs(this.position.y() - target.y());
    }

    /**
     * Проверяет, находится ли цель в радиусе агрессии.
     */
    public boolean isInAggroRange(Position target) {
        return distanceTo(target) <= aggroRange;
    }

    /**
     * Возвращает следующий шаг в направлении к цели (простой алгоритм преследования).
     * Двигается на 1 клетку по оси X или Y, приоритет — по большей дельте.
     */
    public Position getNextStepTowards(Position target) {
        int dx = target.x() - this.position.x();
        int dy = target.y() - this.position.y();

        // Двигаемся по оси с большим смещением
        if (Math.abs(dx) > Math.abs(dy)) {
            return new Position(
                    this.position.x() + Integer.signum(dx),
                    this.position.y()
            );
        } else if (dy != 0) {
            return new Position(
                    this.position.x(),
                    this.position.y() + Integer.signum(dy)
            );
        }

        // Уже на месте
        return this.position;
    }



    public UUID getId() { return id; }
    public Position getPosition() { return position; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getAggroRange() { return aggroRange; }
    public boolean isAlive() { return alive; }
    public char getSymbol() { return symbol; }
    public int getCurrentLevel() { return currentLevel; }
    public int getMapX() { return currentMapGridX; }
    public int getMapY() { return currentMapGridY; }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setMapGrid(int x, int y) {
        this.currentMapGridX = x;
        this.currentMapGridY = y;
    }

    public void setLevel(int level) {
        this.currentLevel = level;
    }

    @Override
    public String toString() {
        return String.format("EnemyNpc[%s, HP:%d/%d, Pos:%s, Alive:%b]",
                id.toString().substring(0, 8), hp, maxHp, position, alive);
    }
}
