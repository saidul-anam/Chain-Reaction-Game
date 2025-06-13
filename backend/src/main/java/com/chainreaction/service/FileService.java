package com.chainreaction.service;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.chainreaction.model.Board;
import com.chainreaction.model.Cell;
@Service
public class FileService {
   private static final String FILE_PATH="gamestate.txt";
public Board readGameState() throws IOException {
    System.out.println(" i am here at read game state");
   Board board=new Board();
    File file = new File(FILE_PATH);
        if (!file.exists()) {
            writeGameState("Human Move:", board);
            return board;
        }
   BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
   String currentOwner = br.readLine();
   if(currentOwner==null){
    br.close();
    throw new IOException("game state.txt file is empyty");
   }
   
 for(int i=0;i<Board.rows;i++){
    String row=br.readLine();
    if(row==null){
        br.close();
        throw new IOException("game state.txt file is empty");
    }
    String[] cells=row.split(" ");
    if(cells.length!=Board.columns){
        br.close();
        throw new IOException("game state.txt file is incomplete or corrupted");
    }
    for (int j=0;j<Board.columns;j++){
        String value=cells[j];
        Cell cell=new Cell();
        if(!value.equals("0")){
            int val=Integer.parseInt(value.substring(0, value.length()-1));
            //System.out.println(val);
            int owner=0;
            if(value.charAt(1)=='R'){
                owner=1;
            }
            else{
                owner=2;
            }
         cell.setOwner(owner);
         cell.setValue(val); 
        }
       board.setCell(i,j,cell); 
    }
 }  
 br.close();
 System.out.println("i am out from read game state");
 return board;
    

}
public void writeGameState(String header,Board board) throws IOException {
    System.out.println("Writing game state...");
BufferedWriter bw=new BufferedWriter(new FileWriter(FILE_PATH,false));
System.out.println("I am now here at write game state");
bw.write(header);
bw.newLine();
board.print();
bw.write(board.toString());
bw.close();
  System.out.println("Writing done game state...");
}
}