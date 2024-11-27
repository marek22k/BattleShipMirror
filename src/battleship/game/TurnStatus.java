package battleship.game;

/**
 * Gibt an, wer gerade dran ist beziehungsweise, ob das Spiel bereits eröffnet
 * ist.
 */
public enum TurnStatus {
    /**
     * Es existiert eine nicht initalisierte Spiele-Sitzung. Dies bedeutet, dass
     * lediglich sehr grundlegende Dinge gesetzt sind. Es wurden noch keine
     * Netzwerkpakete ausgetauscht. Status vor IAM- und COIN-Paket.
     */
    NOT_READY_NOT_INITIALIZED,

    /**
     * Die andere Instanz wurde über uns, unsere Kompatibilität und unseren
     * Spielernamen informiert. Wir sind bereit Befehle von der anderen Instanz
     * entgegen zu nehmen. Das eigentliche Spiel hat noch nicht begonnen. Es wurden
     * noch keine Schiffe platziert. Nach Senden des IAM-Pakets, vor dem Senden des
     * COIN-Pakets.
     */
    NOT_READY_HANDSHAKE_PHASE1_PERFORMED,

    /**
     * Die andere Instanz hat uns Informationen über sie gesendet. Wir haben unsere
     * Schiffe platziert und die andere Instanz darüber informiert. Das Spiel kann
     * erst beginnen wenn die andere Instanz auch ihre Schiffe platziert hat. Nach
     * dem Senden des IAM- und COIN-Paketes. Nach Empfang des IAM-Pakets, vor
     * Empfang des COIN-Pakets.
     */
    NOT_READY_HANDSHAKE_PHASE2_PERFORMED,

    MY_TURN_FIRST_TURN,

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

    YOUR_TURN_FIRST_TURN,

    /**
     * Der Gegner ist dran.
     */
    YOUR_TURN,

    /**
     * Der Gegner ist dran, nachdem er im letzten Zug getroffen hat.
     */
    YOUR_TURN_AFTER_HIT
}
