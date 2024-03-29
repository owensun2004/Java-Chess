package com.chess.engine.board;

import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

import static com.chess.engine.board.Board.*;

public abstract class Move {


    final Board board;
    final Piece movedPiece;
    final int destinationCoordinate;

    public static final Move NULL_MOVE= new NullMove();

    private Move(final Board board,
                 final Piece movedPiece,
                 final int destinationCoordinate) {
        this.board = board;
        this.movedPiece=movedPiece;
        this.destinationCoordinate=destinationCoordinate;
    }
    @Override
    public int hashCode(){
        final int prime=31;
        int result=1;

        result=prime*result+this.destinationCoordinate;
        result=prime*result + this.movedPiece.hashCode();
        return result;
    }
    @Override
    public boolean equals(final Object other){
        if(this==other){
            return true;
        }
        if(!(other instanceof Move)){
            return false;
        }
        final Move otherMove = (Move) other;
        return getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
               getMovedPiece().equals(otherMove.getMovedPiece());

    }
    public int getCurrentCoordinate(){
        return this.getMovedPiece().getPiecePosition();
    }

    public int getDestinationCoordinate(){
        return this.destinationCoordinate;
    }
    public Piece getMovedPiece(){
        return this.movedPiece;
    }
    public boolean isAttack(){
        return false;
    }
    public boolean isCastlingMove(){
        return false;
    }
    public Piece getAttackedPiece(){
        return null;
    }

    public Board execute() {
        //this code creates a new board after a player makes a move using the builder in java
        final Builder builder=new Builder();
        for(final Piece piece : this.board.currentPlayer().getActivePieces()){
            //if the pieces are not the moved pieces, then keep their locations the same in the new board
            if(!this.movedPiece.equals(piece)){
                builder.setPiece(piece);
            }
        }
        //place the same locations for the opponent's pieces on the new board
        for(final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
            builder.setPiece(piece);
        }
        //move the moved piece in the new board
        builder.setPiece(this.movedPiece.movePiece(this));
        //change the next move to the opponent cuz it's their turn to move
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        return builder.build();
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board,
                         final Piece movedPiece,
                         final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

    }
    public static class AttackMove extends Move{
        final Piece attackedPiece;
        public AttackMove(final Board board,
                   final Piece movedPiece,
                   final int destinationCoordinate,
                   final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece=attackedPiece;
        }
        @Override
        public int hashCode(){
            return this.attackedPiece.hashCode()+super.hashCode();
        }
        @Override
        public boolean equals(final Object other){
            if(this==other){
                return true;
            }
            if(!(other instanceof AttackMove)){
                return false;
            }
            final AttackMove otherAttackMove = (AttackMove) other;
            return super.equals(otherAttackMove)&&getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public Board execute() {
            return null;
        }
        @Override
        public boolean isAttack(){
            return true;
        }
        @Override
        public Piece getAttackedPiece(){
            return this.attackedPiece;
        }
    }

    public static final class PawnMove extends Move {

        public PawnMove(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

    }
    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(final Board board,
                              final Piece movedPiece,
                              final int destinationCoordinate,
                              final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

    }
    public static final class PawnEnPassantAttackMove extends PawnAttackMove {

        public PawnEnPassantAttackMove(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate,
                        final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

    }
    public static final class PawnJump extends Move {

        public PawnJump(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }
        //if a pawn jump occurs, I am going to mark that pawn as the enpassant pawn. If the pawn jump piece is not null, then the prior move is a pawn jump
        @Override
        public Board execute(){
            final Builder builder=new Builder();
            for(final Piece piece: this.board.currentPlayer().getActivePieces()){
                if(!this.movedPiece.equals(piece)){
                    builder.setPiece(piece);
                }
            }
            for(final Piece piece: this.board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            final Pawn movedPawn=(Pawn)this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }
    static abstract class CastleMove extends Move {
        protected final Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;

        public CastleMove(final Board board,
                          final Piece movedPiece,
                          final int destinationCoordinate,
                          final Rook castleRook,
                          final int castleRookStart,
                          final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook=castleRook;
            this.castleRookStart=castleRookStart;
            this.castleRookDestination=castleRookDestination;
        }
        public Rook getCastleRook(){
            return this.castleRook;
        }
        @Override
        public boolean isCastlingMove(){
            return true;
        }
        @Override
        public Board execute(){
            final Builder builder=new Builder();
            for(final Piece piece: this.board.currentPlayer().getActivePieces()){
                //if the piece that we are moving on the board is not the moved piece and it's not the castle rook
                if(!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)){
                    //then set the piece
                    builder.setPiece(piece);
                }
            }
            for(final Piece piece: this.board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            //moved piece is king and created a new castle side rook in the new board and erased the old rook
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

    }
    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board,
                                  final Piece movedPiece,
                                  final int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookStart,
                                  final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }
        @Override
        public String toString(){
            return "O-O";
        }
    }
    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board,
                                   final Piece movedPiece,
                                   final int destinationCoordinate,
                                   final Rook castleRook,
                                   final int castleRookStart,
                                   final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }
        @Override
        public String toString(){
            return "O-O-O";
        }
    }
    public static final class NullMove extends Move {

        public NullMove() {
            super(null, null, -1);
        }
        @Override
        public Board execute(){
            throw new RuntimeException("Cannot execute the null move!");
        }
    }
    public static class MoveFactory {
        private MoveFactory(){
            throw new RuntimeException("Not instantiable!");
        }
        public static Move createMove(final Board board,
                                      final int currentCoordinate,
                                      final int destinatinoCoordinate){
            for(final Move move: board.getAllLegalMoves()){
                if(move.getCurrentCoordinate()== currentCoordinate &&
                move.getDestinationCoordinate()== destinatinoCoordinate){
                    return move;
                }
            }
            return NULL_MOVE;
        }

    }
}
