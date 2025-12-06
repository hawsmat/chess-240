package webSocketMessages.userCommands;

public class Leave extends UserGameCommand {

    int gameID;

    public Leave(String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
        this.gameID = gameID;
    }

    public int getGameID() {
        return gameID;
    }
}