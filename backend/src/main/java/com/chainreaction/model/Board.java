package com.chainreaction.model;

public class Board {
    public static final int rows=9;
    public static final int columns=6;
    private Cell[][] board;
    public Board(){
        board=new Cell[rows][columns];
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                board[i][j]=new Cell();
            }
        }
    }
    public Cell[][] getBoard(){
        return board;
    } 
 public Cell getCell(int row,int column){  
    if(row<0||row>=rows||column<0||column>=columns){
        return null;
    }
    return board[row][column];
 }

 public void setCell(int row,int column,Cell cell){
    if(row>=0&&row<rows&&column>=0&&column<columns){
        board[row][column]=cell;
    }
 }
public int getCriticalMass(int row,int column){
int count=0;
if(row>0)count++;
if(row<rows-1)count++;
if(column>0)count++;
if(column<columns-1)count++;
return count;
}

@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
            sb.append(board[i][j].toString());
            if (j < columns - 1) {
                sb.append(" ");
            }
        }
        if (i < rows - 1) {
            sb.append("\n");
        }
    }
    return sb.toString();
}
 public void print(){
    for(int i=0;i<rows;i++){
        for(int j=0;j<columns;j++){
            System.out.print(board[i][j]);
            System.out.print(" ");
        }
        System.out.println();
    }
 }
 public boolean isValidMove(int row,int column,int player){
     if(row<0||row>=rows||column<0||column>=columns){
        return false;
    }
    Cell cell=board[row][column];
    if(cell.getOwner()==0||cell.getOwner()==player){
        return true;
    }
    return false;
}
private void explodeCell(int row,int column){
    Cell cell = board[row][column];
    int critMass=getCriticalMass(row, column);
    int owner=cell.getOwner();
    int value=cell.getValue();

    cell.setValue(value-critMass);
    if(cell.getValue()==0){
        cell.reset();
    }

    int [][]directions={{1,0},{-1,0},{0,1},{0,-1}};
    for(int i=0;i<4;i++){
            int newRow=row+directions[i][0];
            int newColumn=column+directions[i][1];
            if(newRow>=0 && newRow<rows && newColumn>=0 &&newColumn<columns){
                Cell newCell=board[newRow][newColumn];
                newCell.setOwner(owner);
                newCell.setValue(newCell.getValue()+1);
            }

        }
    }
private void checkExplode(int num){
    if(num==0) return ;
    boolean explode=false;
    for(int i=0;i<rows;i++){
        for(int j=0;j<columns;j++){
          Cell cell=board[i][j];
          if(cell.getValue()>=getCriticalMass(i,j)){
            explode=true;
            explodeCell(i,j);
        }
    }
}
if(explode==true&&!isGameOver()) checkExplode(num-1);
}
public void setNewValue(int row,int column,int player){
    if(!isValidMove(row, column, player)){
        System.out.println(" invalid here at setNewValue");
        throw new IllegalArgumentException("Invalid move");
    }

    Cell cell=board[row][column];
    cell.setOwner(player);  
    cell.setValue(cell.getValue()+1);

  checkExplode(200);
}

public Board copy(){
    Board copys = new Board();
    for(int i=0;i<rows;i++){
        for(int j=0;j<columns;j++){
            Cell cell=board[i][j];
            Cell newCell=new Cell();
            newCell.setValue(cell.getValue());
            newCell.setOwner(cell.getOwner());
            copys.setCell(i,j,newCell);
        }
    }
return copys;
}
 public boolean isGameOver(){
    boolean red=false,blue=false;
    for(int i=0;i<rows;i++){
        for(int j=0;j<columns;j++){
            if(board[i][j].getOwner()==1) red=true;
            else if(board[i][j].getOwner()==2)blue=true;
        }
    }
    if((red &&!blue)|| ( !red && blue )){
        return true;
    }
    else {
        return false;
    }
 }

 public int getWinner(){
 if(isGameOver()){
      boolean red=false,blue=false;
    for(int i=0;i<rows;i++){
        for(int j=0;j<columns;j++){
            if(board[i][j].getOwner()==1) red=true;
            else if(board[i][j].getOwner()==2)blue=true;
        }
    }
    if(red) return 1;
    else if(blue) return 2;
 }
return 0;
 }

}