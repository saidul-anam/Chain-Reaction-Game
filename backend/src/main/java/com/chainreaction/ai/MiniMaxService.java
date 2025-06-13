package com.chainreaction.ai;
import com.chainreaction.model.Board;
public class MiniMaxService {
    private HeuristicService heuristicService=new HeuristicService();
    public Move minimax(Board board,int depth,int alpha,int beta,boolean maxMizePlayer,int player,String heuristic){
    if(depth==0){
        int score=heuristicService.calculate(board,player,heuristic);
       // System.out.println("i am at depth 0 now");
       return  new Move(-1,-1,score);
    }
  Move best;
  if(maxMizePlayer){
    best=new Move(-1,-1,Integer.MIN_VALUE);
    //    System.out.println("i am here idiot");
  }
  else{
    best=new Move(-1,-1,Integer.MAX_VALUE);
   // System.out.println("i am here idiot 1");
  }
 // System.out.println(" i am here now and best value right now is  "+best.score);

  for(int i=0;i<board.rows;i++){
    for(int j=0;j<board.columns;j++){
        int otherplayer;
        if(player==1){
            otherplayer=2;
        }
        else{
            otherplayer=1;
        }
        //System.out.println("other player is "+otherplayer);
        if(board.isValidMove(i, j,player)){
          //  System.out.println(" i am now in validation check");
            Board copy=board.copy();
            copy.setNewValue(i,j,player);
            Move nextMove=minimax(copy,depth-1,alpha,beta,!maxMizePlayer,otherplayer,heuristic);
            nextMove.row=i;
            nextMove.column=j;
            if(maxMizePlayer){
                if(nextMove.score>best.score){
                   // System.out.println("am i here?");
                    best=nextMove;
                }
                 //System.out.println("best move is in "+best.row+ " "+best.column);
                    alpha=Math.max(alpha,best.score);

            }
            else{ 
                if(nextMove.score<best.score){
                   //    System.out.println("am i here or not ?");
                    best=nextMove;
                }
                //System.out.println("best move is in "+best.row+ " "+best.column);
                beta=Math.min(beta,best.score);
            }
            if(alpha>=beta){
                break;
            }
        }
    }
    if(alpha>=beta){
        break;
    }
  }
  //System.out.println(" i am now here at the end of minimax");
  //System.out.println(best.row+" "+best.column);
    return best;
}


}
