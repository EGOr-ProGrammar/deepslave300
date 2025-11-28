package server.model;

import shared.Position;

import java.util.Random;

/**
 * Модель игрока.
 * Содержит положение игрока на карте; его цвет, видимый другими игроками.
 */
public class Player {
    private Position position;
    private final int colorCode; // ANSI код цвета

    // Исключен зеленый (32, 92), тк это цвет локального игрока
    private static final int[] ALLOWED_COLORS = {
            31, // Red
            33, // Yellow
            34, // Blue
            35, // Magenta
            36, // Cyan
            91, // Bright Red
            93, // Bright Yellow
            94, // Bright Blue
            95, // Bright Magenta
            96  // Bright Cyan
    };

    public Player(Position startPos) {
        this.position = startPos;
        this.colorCode = ALLOWED_COLORS[new Random().nextInt(ALLOWED_COLORS.length)];
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getColorCode() {
        return colorCode;
    }
}

