package battleship.game;

/**
 * Gibt an, wer gerade dran ist beziehungsweise, ob das Spiel bereits eröffnet
 * ist.
 */
public enum TurnStatus {
    /**
     * Das Spiel ist noch nicht bereit zum start (vor IAM-Paket).
     */
    NOT_READY,

    /**
     * Das Spiel ist vorbereitet (Nach IAM-Paket, vor COIN-Paket).
     */
    PREPARED,

    /**
     * Wir sind dran.
     */
    MY_TURN,

    /**
     * Wir sind dran, da wir im letzten Zug getroffen haben.
     */
    MY_TURN_AFTER_HIT,

    /**
     * Keiner ist dran. Wir warten, dass der Gegner uns nach unserem Angriff
     * antwortet und mitteilt, ob wir getroffen haben.
     */
    WAITING_FOR_REPLY_AFTER_HIT,

    /**
     * Der Gegner ist dran.
     */
    YOUR_TURN,

    /**
     * Der Gegner ist dran, nachdem er im letzten Zug getroffen hat.
     */
    YOUR_TURN_AFTER_HIT
}
