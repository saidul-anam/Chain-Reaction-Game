// src/App.js
import React, { useEffect, useState } from "react";
import "./App.css";

const ROWS = 9;
const COLS = 6;

const AI_TYPES = {
  defensive: { name: "Defensive", icon: "🛡️", description: "Focuses on blocking opponent moves" },
  attacking: { name: "Attacking", icon: "⚔️", description: "Aggressive offensive strategy" },
  control: { name: "Control", icon: "🎯", description: "Territory control focused" },
  balanced: { name: "Balanced", icon: "⚖️", description: "Well-rounded strategy" }
};

const GAME_MODES = {
  humanVsAi: { name: "Human vs AI", icon: "👤🤖" },
  aiVsAi: { name: "AI vs AI", icon: "🤖🤖" }
};

function App() {
  const [gameState, setGameState] = useState('setup'); // 'setup', 'playing', 'finished'
  const [gameMode, setGameMode] = useState(null);
  const [player1AI, setPlayer1AI] = useState('balanced');
  const [player2AI, setPlayer2AI] = useState('defensive');
  const [boardData, setBoardData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [gameOver, setGameOver] = useState(false);
  const [winner, setWinner] = useState(null);
  const [lastMove, setLastMove] = useState(null);
  const [currentPlayer, setCurrentPlayer] = useState(1);
  const [moveCount, setMoveCount] = useState(0);

  const fetchBoard = async () => {
    try {
      const res = await fetch("https://chain-reaction-game.onrender.com/api/game/state");
      if (!res.ok) throw new Error("Failed to fetch game state");
      const data = await res.json();
      setBoardData(data.board);
      setGameOver(data.gameOver || false);
      setWinner(data.winner || null);
      setError("");
    } catch (err) {
      setError("Failed to fetch board: " + err.message);
    }
  };

  // Auto-play for AI vs AI mode
  useEffect(() => {
    if (gameState === 'playing' && gameMode === 'aiVsAi' && !gameOver && !loading) {
      const timer = setTimeout(() => {
        makeAIMove();
      }, 1500);
      return () => clearTimeout(timer);
    }
  }, [gameState, gameMode, gameOver, loading, boardData, moveCount]);

  const startGame = async () => {
    if (!gameMode) return;
    
    setLoading(true);
    setError("");
    
    try {
      // Reset move count
      setMoveCount(0);
      
      // Determine the reset parameter based on game mode
      // Controller expects "Ai" for AI vs AI mode, "Human" for Human vs AI
      const resetParam = gameMode === 'aiVsAi' ? 'Ai' : 'Human';
      
      const res = await fetch(`https://chain-reaction-game.onrender.com/api/game/reset?name=${resetParam}`, {
        method: "POST"
      });
      
      if (!res.ok) throw new Error("Failed to start game");
      const data = await res.json();
      
      setBoardData(data.board);
      setGameOver(false);
      setWinner(null);
      setLastMove(null);
      setCurrentPlayer(1);
      setGameState('playing');
    } catch (err) {
      setError("Failed to start game: " + err.message);
    }
    setLoading(false);
  };

  const makeHumanMove = async (row, col) => {
    if (loading || gameOver || gameMode === 'aiVsAi') return;
    setLoading(true);
    setError("");
    setLastMove(null);

    try {
      const humanRes = await fetch(
        "https://chain-reaction-game.onrender.com/api/game/human-move",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ row, column: col }),
        }
      );
      
      const humanData = await humanRes.json();

      if (!humanRes.ok) {
        setError(humanData.error || "Invalid move");
        setLoading(false);
        return;
      }

      setBoardData(humanData.board);
      setGameOver(humanData.gameOver || false);
      setWinner(humanData.winner);
      setLastMove(humanData.lastMove);
      setMoveCount(prev => prev + 1);
      
      // Switch to AI turn
      setCurrentPlayer(2);
      
      if (!humanData.gameOver) {
        // Make AI move after a short delay
        setTimeout(async () => {
          await makeAIMove();
        }, 800);
      } else {
        setLoading(false);
      }
    } catch (err) {
      setError("Move failed: " + err.message);
      setLoading(false);
    }
  };

  const makeAIMove = async () => {
    if (gameOver) return;
    
    setLoading(true);
    try {
      // Determine which AI type to use based on game mode and current player
      let heuristicType;
      if (gameMode === 'aiVsAi') {
        // For AI vs AI, alternate between the two AI types
        heuristicType = currentPlayer === 1 ? player1AI : player2AI;
      } else {
        // For Human vs AI, always use player2AI (the AI opponent)
        heuristicType = player2AI;
      }
      
      const aiRes = await fetch(`https://chain-reaction-game.onrender.com/api/game/ai-move?Heuristic=${heuristicType}`);
      
      if (!aiRes.ok) throw new Error("AI move failed");
      const aiData = await aiRes.json();
      
      setBoardData(aiData.board);
      setGameOver(aiData.gameOver || false);
      setWinner(aiData.winner);
      setLastMove(aiData.lastMove);
      setMoveCount(prev => prev + 1);
      
      // Switch player for next turn
      if (gameMode === 'aiVsAi' && !aiData.gameOver) {
        setCurrentPlayer(prev => prev === 1 ? 2 : 1);
      } else if (gameMode === 'humanVsAi' && !aiData.gameOver) {
        setCurrentPlayer(1); // Back to human
      }
      
    } catch (aiErr) {
      setError("AI move failed: " + aiErr.message);
    }
    setLoading(false);
  };

  const resetGame = () => {
    setGameState('setup');
    setGameMode(null);
    setBoardData(null);
    setGameOver(false);
    setWinner(null);
    setLastMove(null);
    setCurrentPlayer(1);
    setMoveCount(0);
    setError("");
    setLoading(false);
  };

  const getCellColor = (owner) => {
    if (owner === 1) return "#e74c3c";
    if (owner === 2) return "#3498db";
    return "#ecf0f1";
  };

  const isLastMove = (i, j) =>
    lastMove && lastMove.row === i && lastMove.col === j;

  const getTotalOrbs = (owner) => {
    if (!boardData?.board) return 0;
    return boardData.board.flat().reduce((sum, cell) => {
      return cell.owner === owner ? sum + cell.value : sum;
    }, 0);
  };

  const getCurrentPlayerName = () => {
    if (gameMode === 'humanVsAi') {
      return currentPlayer === 1 ? 'Your' : `AI (${AI_TYPES[player2AI].name})`;
    } else {
      return currentPlayer === 1 
        ? `AI 1 (${AI_TYPES[player1AI].name})` 
        : `AI 2 (${AI_TYPES[player2AI].name})`;
    }
  };

  const getWinnerDisplay = () => {
    if (!winner) return '';
    
    // Handle different winner formats from controller
    if (winner === 'human') {
      return 'You Win!';
    } else if (winner === 'ai') {
      if (gameMode === 'humanVsAi') {
        return `AI (${AI_TYPES[player2AI].name}) Wins!`;
      } else {
        return 'AI Wins!';
      }
    } else if (winner === 'AI 1') {
      return `AI 1 (${AI_TYPES[player1AI].name}) Wins!`;
    } else if (winner === 'AI 2') {
      return `AI 2 (${AI_TYPES[player2AI].name}) Wins!`;
    }
    
    return `${winner} Wins!`;
  };

  if (gameState === 'setup') {
    return (
      <div className="app-container">
        <header className="app-header">
          <h1>⚡ Chain Reaction ⚡</h1>
          <p className="subtitle">Choose your battle configuration</p>
        </header>

        {error && <div className="error-box">⚠️ {error}</div>}

        <div className="setup-container">
          <div className="setup-section">
            <h3>Game Mode</h3>
            <div className="mode-selector">
              {Object.entries(GAME_MODES).map(([key, mode]) => (
                <button
                  key={key}
                  className={`mode-button ${gameMode === key ? 'selected' : ''}`}
                  onClick={() => setGameMode(key)}
                >
                  <div className="mode-icon">{mode.icon}</div>
                  <div className="mode-name">{mode.name}</div>
                </button>
              ))}
            </div>
          </div>

          {gameMode && (
            <div className="setup-section">
              <h3>AI Configuration</h3>
              
              {gameMode === 'aiVsAi' && (
                <div className="ai-config">
                  <div className="player-config">
                    <h4>🤖 Player 1 AI</h4>
                    <div className="ai-selector">
                      {Object.entries(AI_TYPES).map(([key, ai]) => (
                        <button
                          key={key}
                          className={`ai-button ${player1AI === key ? 'selected' : ''}`}
                          onClick={() => setPlayer1AI(key)}
                        >
                          <div className="ai-icon">{ai.icon}</div>
                          <div className="ai-name">{ai.name}</div>
                          <div className="ai-desc">{ai.description}</div>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              <div className="ai-config">
                <div className="player-config">
                  <h4>🤖 {gameMode === 'aiVsAi' ? 'Player 2 AI' : 'AI Opponent'}</h4>
                  <div className="ai-selector">
                    {Object.entries(AI_TYPES).map(([key, ai]) => (
                      <button
                        key={key}
                        className={`ai-button ${player2AI === key ? 'selected' : ''}`}
                        onClick={() => setPlayer2AI(key)}
                      >
                        <div className="ai-icon">{ai.icon}</div>
                        <div className="ai-name">{ai.name}</div>
                        <div className="ai-desc">{ai.description}</div>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {gameMode && (
            <div className="start-container">
              <button
                onClick={startGame}
                disabled={loading}
                className="start-button"
              >
                {loading ? "🔄 Starting..." : "🚀 Start Game"}
              </button>
            </div>
          )}
        </div>

        <div className="instructions">
          <strong>How to Play:</strong>
          <ul>
            <li>Click on empty cells or your own cells to place orbs.</li>
            <li>When a cell reaches critical mass, it explodes.</li>
            <li>Explosions spread orbs and convert opponents.</li>
            <li>Eliminate all opponent orbs to win!</li>
          </ul>
        </div>
      </div>
    );
  }

  if (!boardData) {
    return (
      <div className="loading-screen">
        <div className="loading-content">
          <div className="loading-icon">🔄</div>
          <div>Loading Chain Reaction...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>⚡ Chain Reaction ⚡</h1>
        <div className="game-info">
          <div className="mode-display">
            {GAME_MODES[gameMode].icon} {GAME_MODES[gameMode].name}
          </div>
          {gameMode === 'aiVsAi' && (
            <div className="ai-display">
              🤖 {AI_TYPES[player1AI].icon} vs {AI_TYPES[player2AI].icon} 🤖
            </div>
          )}
        </div>
        
        {!gameOver && (
          <p className={loading ? "status thinking" : "status turn-info"}>
            {loading 
              ? `🤖 ${getCurrentPlayerName()} is thinking...` 
              : `🎮 ${getCurrentPlayerName()} turn!`
            }
          </p>
        )}
        {gameOver && (
          <p className={`status ${winner === 'human' ? 'you-win' : 'ai-win'}`}>
            🎉 {getWinnerDisplay()} 🎉
          </p>
        )}
      </header>

      {error && <div className="error-box">⚠️ {error}</div>}

      <div className="board">
        {boardData.board.map((row, i) =>
          row.map((cell, j) => {
            const owner = cell.owner || 0;
            const value = cell.value || 0;
            const isLast = isLastMove(i, j);
            return (
              <div
                key={`${i}-${j}`}
                className={`cell owner-${owner} ${isLast ? "last-move" : ""}`}
                onClick={() => gameMode === 'humanVsAi' && currentPlayer === 1 && !loading && !gameOver && makeHumanMove(i, j)}
              >
                {value > 0 ? (
                  <div className="orbs">
                    {Array.from({ length: value }).map((_, k) => (
                      <span key={k} className={`orb orb-${owner}`} />
                    ))}
                  </div>
                ) : (
                  <span className="plus-sign">+</span>
                )}
              </div>
            );
          })
        )}
      </div>

      <div className="stats">
        <div className="player-stat">
          <div className="player-label">
            {gameMode === 'humanVsAi' ? '👤 You' : `🤖 ${AI_TYPES[player1AI].name}`}
          </div>
          <div className="count count-human">{getTotalOrbs(1)}</div>
        </div>
        <div className="player-stat">
          <div className="player-label">
            🤖 {AI_TYPES[player2AI].name}
          </div>
          <div className="count count-ai">{getTotalOrbs(2)}</div>
        </div>
      </div>

      <div className="game-controls">
        <button onClick={resetGame} disabled={loading} className="reset-button">
          🔄 New Game
        </button>
      </div>
    </div>
  );
}

export default App;
