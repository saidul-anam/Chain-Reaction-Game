package com.chainreaction.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chainreaction.ai.MiniMaxService;
import com.chainreaction.ai.Move;
import com.chainreaction.model.Board;
import com.chainreaction.service.FileService;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private FileService fileService;
    private MiniMaxService minimax = new MiniMaxService();
    private final int AI_PLAYER = 2;
    private final int HUMAN_PLAYER = 1;
    private int moveCount = 0;
    private boolean isAiVsAi = false;
    private int currentPlayer = 1;
    
    @GetMapping("/ai-move")
    public ResponseEntity<Map<String, Object>> makeAIMove(@RequestParam String Heuristic) throws IOException {
        Board board = fileService.readGameState();
        Map<String, Object> response = new HashMap<>();
        System.out.println("AI move - Move count: " + moveCount + ", Current player: " + currentPlayer + ", AI vs AI: " + isAiVsAi);
        if (board.isGameOver()&&moveCount>1) {
            response.put("board", board);
            response.put("gameOver", true);
            
            int winner = board.getWinner();
            if (isAiVsAi) {
                if (winner == 1) {
                    response.put("winner", "AI 1");
                } else {
                    response.put("winner", "AI 2");
                }
            } else {
                if (winner == 1) {
                    response.put("winner", "human");
                } else {
                    response.put("winner", "ai");
                }
            }
            return ResponseEntity.ok(response);
        }     
        moveCount++;
        int playerForMove;
        if (isAiVsAi) {
            playerForMove = currentPlayer;
        } else {
            playerForMove = AI_PLAYER;
        }
        Move bestMove = minimax.minimax(board, 2, Integer.MIN_VALUE, Integer.MAX_VALUE, true, playerForMove, Heuristic);
        System.out.println("Best move: " + bestMove.row + ", " + bestMove.column + " for player " + playerForMove);
        if (bestMove.row == -1 || bestMove.column == -1) {
            response.put("board", board);
            response.put("gameOver", true);
            
            if (isAiVsAi) {
                String winner = (currentPlayer == 1) ? "AI 2" : "AI 1";
                response.put("winner", winner);
            } else {
                response.put("winner", "human");
            }
            return ResponseEntity.ok(response);
        }
        board.setNewValue(bestMove.row, bestMove.column, playerForMove);
        String moveDescription;
        if (isAiVsAi) {
            moveDescription = "AI " + currentPlayer + " Move:";
        } else {
            moveDescription = "AI Move:";
        }
        fileService.writeGameState(moveDescription, board);
        response.put("board", board);
        if(moveCount>1){
        response.put("gameOver", board.isGameOver());}
        else{
            response.put("gameOver", false);
        }
        System.out.println("Game over at line 89");
        if (board.isGameOver()&&moveCount>1) {
            System.out.println("Game over ");
            int winner = board.getWinner();
            if (isAiVsAi) {
                if (winner == 1) {
                    response.put("winner", "AI 1");
                } else {
                    response.put("winner", "AI 2");
                }
            } else {
                if (winner == 1) {
                    response.put("winner", "human");
                } else {
                    response.put("winner", "ai");
                }
            }
        }    
        response.put("lastMove", Map.of("row", bestMove.row, "col", bestMove.column, "player", playerForMove));
        if (isAiVsAi) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/human-move")
    public ResponseEntity<Map<String, Object>> makeHumanMove(@RequestBody Move move) throws IOException {
        Board board = fileService.readGameState();
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("Human move - Move count: " + moveCount);
        
        if (board.isGameOver()&&moveCount>1) {
            System.out.println("Game over ");
            response.put("error", "Game is already over");
            response.put("board", board);
            response.put("gameOver", true);
            response.put("winner", "ai");
            return ResponseEntity.badRequest().body(response);
        }

        if (!board.isValidMove(move.row, move.column, HUMAN_PLAYER)) {
            response.put("error", "Invalid move");
            response.put("board", board);
            return ResponseEntity.badRequest().body(response);
        }

        moveCount++;
        board.setNewValue(move.row, move.column, HUMAN_PLAYER);
        fileService.writeGameState("Human Move:", board);
        
        response.put("board", board);
        if(moveCount>1){
        response.put("gameOver", board.isGameOver());}
        else{
            response.put("gameOver", false);
        }
        System.out.println("Game over at line 142 ");
        if (board.isGameOver()&&moveCount>1) {
            System.out.println("Game over ");
            int winner = board.getWinner();
            if (winner == 1) {
                response.put("winner", "human");
            } else {
                response.put("winner", "ai");
            }
        }
        
        response.put("lastMove", Map.of("row", move.row, "col", move.column, "player", HUMAN_PLAYER));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getGameState() throws IOException {
        Board board = fileService.readGameState();
        Map<String, Object> response = new HashMap<>();
        System.out.println(" i am currently in state");
        response.put("board", board);
        response.put("gameOver", board.isGameOver());
        System.out.println("Game over 165 ");
        if (board.isGameOver()&&moveCount>0) {
            System.out.println("Game over ");
            int winner = board.getWinner();
            if (isAiVsAi) {
                if (winner == 1) {
                    response.put("winner", "AI 1");
                } else {
                    response.put("winner", "AI 2");
                }
            } else {
                if (winner == 1) {
                    response.put("winner", "human");
                } else {
                    response.put("winner", "ai");
                }
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetGame(@RequestParam String name) throws IOException {
        Board newBoard = new Board();
        moveCount = 0;
        currentPlayer = 1;
        isAiVsAi = name.equals("Ai");
        String resetDescription = isAiVsAi ? "AI vs AI Game Start:" : "Human vs AI Game Start:";
        fileService.writeGameState(resetDescription, newBoard);
        Map<String, Object> response = new HashMap<>();
        response.put("board", newBoard);
        response.put("gameOver", false);
        response.put("winner", null);
        response.put("message", "Game reset successfully");
        System.out.println("Game reset - AI vs AI: " + isAiVsAi);
        return ResponseEntity.ok(response);
    }
}
