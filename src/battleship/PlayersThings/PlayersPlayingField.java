package battleship.PlayersThings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battleship.ui.PlaygroundMatrix.PlaygroundMatrix;

public class PlayersPlayingField {
    private final ArrayList<PlayersShip> ships;
    private final int size;

    public PlayersPlayingField(int size) {
        this.ships = new ArrayList<>();
        this.size = size;
    }

    public boolean allSunk() {
        return this.ships.stream().allMatch(PlayersShip::isSunk);
    }

    public void debugPrint() {
        for (final PlayersShip element : this.ships) {
            System.out.println(element);
        }
    }

    public void generateShips(List<Integer> list) {
        final Random random = new Random();
        for (final Integer shipSize : list) {
            PlayersShip ship;
            do {
                ship = PlayersShip.generate(random, this.size, shipSize);
            } while (!this.isShipSuitable(ship));
            this.placeShip(ship);
        }
    }

    public PlayersShip hit(PlayersField field) {
        for (final PlayersShip ship : this.ships) {
            if (ship.isOnField(field)) {
                ship.hit(field);
                return ship;
            }
        }
        return null;
    }

    public boolean isShipSuitable(PlayersShip ship) {
        for (final PlayersShip element : this.ships) {
            if (element.isNeighborOrEqual(ship)) {
                return false;
            }
        }
        return true;
    }

    public void placeShip(PlayersShip ship) {
        this.ships.add(ship);
    }

    public void print(PlaygroundMatrix pm) {
        for (int y = 0; y < this.size; y++) {
            for (int x = 0; x < this.size; x++) {
                pm.setWater(x, y);
            }
        }

        for (final PlayersShip element : this.ships) {
            element.print(pm);
        }
    }
}
