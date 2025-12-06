package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    final private ChessGame.TeamColor pieceColor;
    final private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        if (board.getPiece(myPosition).getPieceType() == PieceType.BISHOP) {
            moves = calculateDiagonalAndSides(myPosition, board, 1, 1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 1, -1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, -1, 1, moves);
            return calculateDiagonalAndSides(myPosition, board, -1, -1, moves);
        }
        else if (board.getPiece(myPosition).getPieceType() == PieceType.KING) {
            return calculateKing(myPosition, board, moves);
        }
        else if (board.getPiece(myPosition).getPieceType() == PieceType.KNIGHT) {
            return calculateKnight(myPosition, board, moves);
        }
        else if (board.getPiece(myPosition).getPieceType() == PieceType.PAWN) {
            if (pieceColor == ChessGame.TeamColor.WHITE) {
                return calculatePawn(myPosition, board, 1, 2, moves);
            }
            else {
                return calculatePawn(myPosition, board, -1, 7, moves);
            }
        }
        else if (board.getPiece(myPosition).getPieceType() == PieceType.ROOK) {
            moves = calculateDiagonalAndSides(myPosition, board, 0, 1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 0, -1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 1, 0, moves);
            return calculateDiagonalAndSides(myPosition, board, -1, 0, moves);
        }
        else if (board.getPiece(myPosition).getPieceType() == PieceType.QUEEN) {
            moves = calculateDiagonalAndSides(myPosition, board, 1, 1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 1, -1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, -1, 1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, -1, -1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 0, 1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 0, -1, moves);
            moves = calculateDiagonalAndSides(myPosition, board, 1, 0, moves);
            return calculateDiagonalAndSides(myPosition, board, -1, 0, moves);
        }
        return List.of();
    }

    public List<ChessMove> calculateDiagonalAndSides(ChessPosition myPosition, ChessBoard board, int row, int col, List<ChessMove> moves) {
        for (int i = 1; i < 8; i++) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i*row, myPosition.getColumn() + i*col);
            if (isValid(board, newPosition)) {
                if (board.getPiece(newPosition) != null && board.getPiece(newPosition).getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                else {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            else {
                break;
            }
        }
        return moves;
    }

    public List<ChessMove> calculatePawn(ChessPosition myPosition, ChessBoard board, int direction, int startPos, List<ChessMove> moves) {
        int x = myPosition.getRow() + direction;
        int y = myPosition.getColumn();
        ChessPosition newPosition = new ChessPosition(x, y);
        List<PieceType> pieces = Arrays.asList(
                PieceType.ROOK,
                PieceType.QUEEN,
                PieceType.BISHOP,
                PieceType.KNIGHT
        );
        if (x + direction > 8 || x + direction < 1) {
            for (PieceType promotion: pieces) {
                pawnCapture(myPosition, new ChessPosition(x, y+1), promotion, board, moves);
                pawnCapture(myPosition, new ChessPosition(x, y-1),promotion, board, moves);
                pawnUp(myPosition, new ChessPosition(x, y), promotion, board, moves);
            }
        }
        else {
            pawnCapture(myPosition, new ChessPosition(x, y+1), null, board, moves);
            pawnCapture(myPosition, new ChessPosition(x, y-1),null, board, moves);
            pawnUp(myPosition, newPosition, null, board, moves);
            if (x-direction == startPos) {
                pawnUpTwo(myPosition, new ChessPosition(x+direction, y), null, board, direction, moves);
            }
        }
        return moves;
    }

    public List<ChessMove> pawnCapture(ChessPosition myPosition, ChessPosition newPosition, PieceType type, ChessBoard board, List<ChessMove> moves){
        if (newPosition.getColumn() > 0 && newPosition.getColumn() < 9 &&
                newPosition.getRow() > 0 && newPosition.getRow() < 9 && board.getPiece(newPosition) != null &&
                board.getPiece(newPosition).getTeamColor() != pieceColor) {
            moves.add(new ChessMove(myPosition, newPosition, type));
        }
        return moves;
    }

    public List<ChessMove> pawnUp(ChessPosition myPosition, ChessPosition newPosition, PieceType type, ChessBoard board, List<ChessMove> moves){
        if (pawnIsValid(board, newPosition)) {
            moves.add(new ChessMove(myPosition, newPosition, type));
        }
        return moves;
    }

    public List<ChessMove> pawnUpTwo(ChessPosition myPosition, ChessPosition newPosition,
                                     PieceType type, ChessBoard board, int direction, List<ChessMove> moves){
        if (pawnIsValid(board, newPosition) && pawnIsValid(board, new ChessPosition(newPosition.getRow()-direction, newPosition.getColumn()))) {
            moves.add(new ChessMove(myPosition, newPosition, type));
        }
        return moves;
    }

    public boolean pawnIsValid(ChessBoard board, ChessPosition newPosition) {
        return (newPosition.getRow() < 9 && newPosition.getRow() > 0 &&
                newPosition.getColumn() < 9 && newPosition.getColumn() > 0 && board.getPiece(newPosition) == null);
    }

    public List<ChessMove> calculateKing(ChessPosition myPosition, ChessBoard board, List<ChessMove> moves){
        List<List<Integer>> rowsAndCols = Arrays.asList(
                Arrays.asList(-1, -1),
                Arrays.asList(-1, 0),
                Arrays.asList(-1, 1),
                Arrays.asList(0, -1),
                Arrays.asList(0, 1),
                Arrays.asList(1, -1),
                Arrays.asList(1, 0),
                Arrays.asList(1, 1)
        );
        return knightKingHelper(myPosition, board, rowsAndCols, moves);
    }

    public List<ChessMove> knightKingHelper(ChessPosition myPosition, ChessBoard board, List<List<Integer>> rowsAndCols, List<ChessMove> moves){
        for (int i = 0; i < rowsAndCols.size(); i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowsAndCols.get(i).get(0),
                    myPosition.getColumn() + rowsAndCols.get(i).get(1));
            if (isValid(board, newPosition)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + rowsAndCols.get(i).get(0),
                        myPosition.getColumn() + rowsAndCols.get(i).get(1)), null));
            }
        }
        return moves;
    }

    public List<ChessMove> calculateKnight(ChessPosition myPosition, ChessBoard board, List<ChessMove> moves){
        List<List<Integer>> rowsAndCols = Arrays.asList(
                Arrays.asList(-2, -1),
                Arrays.asList(-2, 1),
                Arrays.asList(2, 1),
                Arrays.asList(2, -1),
                Arrays.asList(1, 2),
                Arrays.asList(-1, 2),
                Arrays.asList(1, -2),
                Arrays.asList(-1, -2)
        );
        return knightKingHelper(myPosition, board, rowsAndCols, moves);
    }

    public boolean isValid(ChessBoard board, ChessPosition newPosition) {
        return (newPosition.getRow() < 9 && newPosition.getRow() > 0 &&
                newPosition.getColumn() < 9 && newPosition.getColumn() > 0 &&
                (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != pieceColor));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}