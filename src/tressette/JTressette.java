package tressette;

import tressette.controller.GameController;
import tressette.view.GUI;

public class JTressette {
    public static void main(String[] args) {
        GUI gui = new GUI();
        GameController controller = new GameController(gui);
        controller.startGame();
    }
}