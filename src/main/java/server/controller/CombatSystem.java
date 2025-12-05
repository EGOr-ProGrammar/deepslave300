package server.controller;

import server.model.EnemyNpc;
import server.model.Player;
import shared.Position;

import java.util.Map;
import java.util.UUID;

/**
 * Система боя и взаимодействий игрока с миром
 */
public class CombatSystem {

    /**
     * Атакует врага на целевой позиции, если он там есть
     * @return true если атака произошла, false если нет цели
     */
    public static boolean tryAttackEnemy(Player player, Position targetPos,
                                         Map<UUID, EnemyNpc> enemies) {
        for (EnemyNpc enemy : enemies.values()) {
            if (enemy.isAlive() &&
                    enemy.getCurrentLevel() == player.getCurrentLevel() &&
                    enemy.getMapX() == player.getMapX() &&
                    enemy.getMapY() == player.getMapY() &&
                    enemy.getPosition().equals(targetPos)) {

                enemy.takeDamage(player.getAttack());
                System.out.println("💥 Player hit enemy for " + player.getAttack() + " dmg");
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerAt(Position targetPos, Player currentPlayer,
                                     Map<String, Player> allPlayers) {
        for (Player other : allPlayers.values()) {
            if (other != currentPlayer &&
                    other.getCurrentLevel() == currentPlayer.getCurrentLevel() &&
                    other.getMapX() == currentPlayer.getMapX() &&
                    other.getMapY() == currentPlayer.getMapY() &&
                    other.getPosition().equals(targetPos)) {
                return true;
            }
        }
        return false;
    }
}
