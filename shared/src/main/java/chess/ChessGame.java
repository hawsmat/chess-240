package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;

    private ChessBoard board;

    private boolean gameOver;

    public ChessGame() {
        board = new ChessBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessGame.TeamColor pieceColor = board.getPiece(startPosition).getTeamColor();
        Collection<ChessMove> possibleMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> actualMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            chess.ChessBoard tempBoard = new ChessBoard();
            for (int i = 1; i < 9; i++) {
                for (int j = 1; j < 9; j++) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.getPiece(position) != null) {
                        tempBoard.addPiece(position, new ChessPiece(board.getPiece(position).getTeamColor(),
                                board.getPiece(position).getPieceType()));
                    }
                }
            }
            tempBoard.addPiece(move.getEndPosition(), tempBoard.getPiece(move.getStartPosition()));
            tempBoard.addPiece(move.getStartPosition(), null);
            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(tempBoard);
            if (!tempGame.isInCheck(pieceColor) && !tempGame.isInCheckmate(getTeamTurn())) {
                actualMoves.add(move);
            }
        }
        return actualMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException("There is no piece there");
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != getTeamTurn()){
            throw new InvalidMoveException("That is not your piece");
        }
        if (board.getPiece(move.getEndPosition()) != null && board.getPiece(move.getEndPosition()).getTeamColor() == getTeamTurn()) {
            throw new InvalidMoveException("You can't capture your own pieces");
        }
        Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());
        for (ChessMove possibleMove: possibleMoves) {
            if (move.equals(possibleMove)) {
                if (move.getPromotionPiece() == null) {
                    board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
                    board.addPiece(move.getStartPosition(), null);
                }
                else {
                    board.addPiece(move.getEndPosition(), new ChessPiece(board.getPiece(move.getStartPosition()).getTeamColor(),
                            move.getPromotionPiece()));
                    board.addPiece(move.getStartPosition(), null);
                }
                if (getTeamTurn() == TeamColor.BLACK) {
                    setTeamTurn(TeamColor.WHITE);
                }
                else {
                    setTeamTurn(TeamColor.BLACK);
                }
                return;
            }
        }
        throw new InvalidMoveException("That's not good");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = new ChessPosition(0, 0);
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = position;
                }
            }
        }

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition enemyPosition = new ChessPosition(i, j);
                ChessPiece enemyPiece = board.getPiece(enemyPosition);
                if (enemyPiece == null || enemyPiece.getTeamColor() == teamColor) {
                    continue;
                }
                Collection<ChessMove> otherMoves = board.getPiece(enemyPosition).pieceMoves(board, enemyPosition);
                for (ChessMove move: otherMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    public boolean checkmateStalemateHelper(TeamColor teamColor, boolean inCheckAllowed) {
        if (isInCheck(teamColor) == inCheckAllowed) {
            return false;
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> possibleMoves = validMoves(position);
                    if (!possibleMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return checkmateStalemateHelper(teamColor, false);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return checkmateStalemateHelper(teamColor, true);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                this.board.addPiece(new ChessPosition(i, j), piece);
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        boolean colorIsEqual= teamTurn == chessGame.teamTurn;
        boolean boardIsEqual = Objects.equals(board, chessGame.board);
        return colorIsEqual && boardIsEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
