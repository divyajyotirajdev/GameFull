import static java.lang.System.out;

import java.util.*;
import java.lang.Math;

	public class Fan5x5 {
		int value; 	//final utility value is stored here
		int paika; 	//Number of paika moves updates after each turn of human or computer, 0 intiially
		int pick;	//0: if no Double captures, 1for Approach capture, -1 for Withdraw Capture
		int[][] action;
		int pruneMin;
		int pruneMax;
		int nodesGen;
		
		public Fan5x5(){ //constructor with default values
			value=-2;
			paika=0;
			action=new int[][] {{-1,1},{-1,-1}};
			pruneMin=0;
			pruneMax=0;
			nodesGen=0;
		}
		
		/*
		 * Controls the entire flow of Gameplay
		 * Starts with initializing the board and class variables
		 * takes user choice of player Black or White
		 * Invokes alphabetaSearch for computer player
		 * or Takes user choice for action
		 * after each turn of gameplay flips the turn (B to W and W to B), and flips player (C to H and H to C)
		 */
		public static void main(String[] args) {
			out.println("You will need to enter 2 indexes to make a move"); //instructions for Human Player
			out.println("index1 = position you want to move from ");
			out.println("index2= position you want to move to ");
			out.println("if you enter index1 that does not correspond to your piece you will miss a turn");
			out.println("enter i1,j1, i2, j2 in separate lines");
			Fan5x5 fan = new Fan5x5();
			char[][] config = new char[5][5];
			int[][] actionMain = new int[2][2];
			int captureType;
			char turn='e';
			char player='c';
			turn = fan.getChoice(turn); //asking Human player if he wants to start as Black or White
			boolean continueGame=true;
			//config= new char[][] {{'e','b','e','e','e'},{'e','b','w','e','e'},{'e','e','e','e','e'},{'e','e','e','e','e'},{'e','e','e','e','e'}};
			fan.startConfig(config); //initializing start configuration of board 	
			if (turn =='w') {
				player='h';
			}
			fan.printConfig(config);
			while(continueGame) { //continueGame flag resets if current state is Terminal State
				int fakePaika =fan.getPaika();
				if (player=='c') {
					out.println("The algorithm's turn");
					char[][] configcopy= fan.cloneArray(config);
					fan.alphaBetaSearch(configcopy, fakePaika, turn); //alpha beta is called with a copy of the original board and fake paika value
					actionMain=fan.getAction(); //alphaBeta returns an action which is put in class variable action
				}
				else {
					out.println("Human's turn"); //getting action from Human Player as 2 indices
					Scanner sc = new Scanner(System.in);
					out.println("Enter in1, in2 as specified");
					for(int i=0;i<2;++i){
						int x=sc.nextInt();
						int y=sc.nextInt();
						actionMain[i] = new int[]{x,y};
					}
				}
				int[] in1=actionMain[0];
				int[] in2=actionMain[1];
				if (in1[0]!=-1 && in1[1]!=-1 && config[in1[0]][in1[1]]==turn) {
					if (player=='h') {captureType =0;}
					else captureType=fan.getPick(); //if player is Algorithm, captureType is store by alpha beta Search in class variable pick
					boolean isCap = fan.moveToken(config, in1, in2, turn, player, captureType); 
					if(isCap) {fan.putPaika(0);} //if this is a capture move Reset paika class variable, otherwise increment it
					else {fan.putPaika(fan.getPaika()+1);}
				}
				fan.printConfig(config);
				fan.putValue(-2);	//class variable value is the utility value returned by alphaBetaSearch, default is (-2), allowing update to -1,0,1
				if(fan.terminalTest(config,fan.getPaika())) {
					int utility = fan.utility(config, fan.getPaika(), turn);
					switch(utility) {	//if in terminal state announce result
						case 0: out.println("draw"); break;
						case 1: out.println("Algorithm wins"); break;
						case -1: out.println("Human Player wins"); break;
					}
					continueGame=false;
				}
				turn=fan.flipTurn(turn);	//after each turn of gameplay, flip the turn and flip the player 
				player=fan.flipPlayer(player);
			}
		}
		
		/****************************** AlphaBetaSearch FUNCTIONS *******************************/
		
		/*
		 * function performs alphaBetaSeach as described in the book, calls MaxValue function
		 */
		public void alphaBetaSearch(char[][] state, int paika, char turn){ 
			out.println("Depth of tree ");
			int v = maxValue(state,-Integer.MAX_VALUE,Integer.MAX_VALUE,paika,turn,1);
			out.println();
			out.println("Number of times pruning occured in MaxValue " + getPruneMax());
			out.println("Number of times pruning occured in MinValue " + getPruneMin());
			out.println("Number of nodes generated " + getNodesGen());
			if (v!=getValue()) {out.println("Something's wrong");} //for debugging purpose only
		}
		
		/*
		 * maxValue function checks for terminal state, if terminal returns utility
		 * otherwise calls actionsOfState for the list of actions of state as an ArrayList of int[]
		 * if there are any capture actions actionsOfState will contains only capture actions(Fanorona Rules)
		 * if there are any capture actions, reset paika, otherwise increment paika
		 * call minValue function for each action of state
		 * while backtracking for each action at level 1, if final utility value is greater then putAction, putValue and CaptureType
		 */
		public int maxValue(char[][] state,int alpha,int beta,int paika, char turn, int localLevel){
			out.print(localLevel+" "+(localLevel+1+" "));
			if (terminalTest(state,paika)) return utility(state,paika,turn);
			int v=-Integer.MAX_VALUE, captureType=0;
			List<int[]> actions = actionsOfState(state,turn); //create list of actionsOfState
			int actionSize= actions.size();
			incrementNodesGen(actionSize);
			Iterator<int[]> it = actions.iterator();
			int[][] prevIn={{-1,-1},{-1,-1}};
			boolean isCap=false;
			while(it.hasNext()){
				int[] in1= it.next();
				int[] in2= it.next();
				if (!isNull1x2(in1) &&!isNull1x2(in2)){ //check for captures or doubleCaptures
					boolean isDoubleCap=isDoubleCapture(cloneArray(state),in1,in2,turn);
					if (isDoubleCap) {
						if(!Arrays.deepEquals(prevIn,new int[][] {in1,in2})) captureType=1;
						else captureType=-1;
					}
					else isCap= moveToken(cloneArray(state),in1,in2,turn,'c',captureType);
					prevIn=new int[][] {in1,in2};
					if(!anyCapture(isCap, isDoubleCap)) {paika+=1;} //reset value of paika for capture, otherwise increment
					else paika=0;
					v= Math.max(v, minValue(state,alpha,beta,paika,turn, localLevel+1)); //call minValue function with each action
					if (localLevel==1) {
						if(v>getValue()) { //while backtracking if level =1, set the class variables
							putAction(in1,in2);
							putValue(v);
							putPick(captureType);
						} 
					}
					if (v>=beta) {
						incrementPruneMax();
						return v;
					}
					alpha=Math.max(alpha,v);
				}
			}
			return v;
		}
		
		
		/*
		 * minValue function checks for terminal state, if terminal returns utility
		 * otherwise calls actionsOfState for the list of actions of state as an ArrayList of int[]
		 * if there are any capture actions actionsOfState will contains only capture actions(Fanorona Rules)
		 * if there are any capture actions, reset paika, otherwise increment paika
		 * call maxValue function for each action of state
		 * rest is unchanged as per the logic of the book
		 */
		public int minValue(char[][] state,int alpha, int beta, int paika, char turn, int localLevel) {
			if (terminalTest(state, paika)) {return utility(state, paika, turn);}
			int v=Integer.MAX_VALUE,captureType=0;
			boolean isCap=false;
			int[][] prevIn={{-1,-1},{-1,-1}};
			List<int[]> actions = actionsOfState(state,turn); //create list of actionsOfState
			int actionSize= actions.size();
			incrementNodesGen(actionSize);
			Iterator<int[]> it = actions.iterator();
			while(it.hasNext()){
				int[] in1= it.next();
				int[] in2= it.next();
				if (!isNull1x2(in1) &&!isNull1x2(in2)){ //check for captures or doubleCaptures
					boolean isDoubleCap=isDoubleCapture(cloneArray(state),in1,in2,turn); 
					if (isDoubleCap) { //if double capture and in1 to in2 action is being detected for the first time, try for forward capture
						if(!Arrays.deepEquals(prevIn,new int[][] {in1,in2})) captureType=1;
						else captureType=-1; //if in1 to in2 action has been seen before try for reverse capture
					}
					else isCap= moveToken(state,in1,in2,turn,'c',captureType);
					prevIn=new int[][] {in1,in2};
					if(!anyCapture(isCap, isDoubleCap)) {paika+=1;}
					else paika=0; //reset value of paika for capture, otherwise increment
					v= Math.min(v, maxValue(state,alpha,beta,paika,turn, localLevel+1)); //call maxValue function with each action
					if (v<=alpha) {
						incrementPruneMin();
						return v;
					}
					beta=Math.min(beta,v);
				}
			}
			return v;
		}
		
		/*
		 * Checks for Terminal state which are:
		 * if there are no more pieces of Black or White player
		 * if there are more than 5 consecutive paika moves 
		 */
		public boolean terminalTest(char[][] state, int paika){
			int blackCtr = tokenCtr(state,'b');
			int whiteCtr = tokenCtr(state,'w');
			if (paika>=5 || blackCtr==0 || whiteCtr==0) {return true;}
			return false;
		}
		
		/*
		 * Returns utility related to Terminal State which is
		 * 1: if Max player(turn) wins
		 * -1: if Min player(opponent) wins
		 * 0: Draw if successive Paika moves exceed 5
		 */
		public int utility(char[][] state, int paika, char turn){
			char opponent = flipTurn(turn);
			int opponentCtr = tokenCtr(state,opponent);
			if (paika>=5) {return 0;}
			else if (opponentCtr==0) {return 1;}
			else return -1;
		}
		
		/*
		 * Generate actions of State by looking at each position(index) of current player's pieces
		 * and all the empty positions as its neighbors (index in adjacency)
		 */
		public List<int[]> actionsOfState(char[][] state, char turn) { 
			List<int[]> actions = new ArrayList<int[]>();
			int[][] turnPositions = posOfChar(state,turn); //find all positions of current player(turn) pieces
			int[][] adj;
			for (int[] in: turnPositions){
				if (!isNull1x2(in)) {
					adj = refinedAdjacency(state,in); //find all positions empty on the board as neighbor
					for (int[] in2: adj) {
						if (!isNull1x2(in2)) {
							actions.add(in); 	//add index of current player, index of next empty position in the actions list
							actions.add(in2);
						}
					}
				}
			}
			List<int[]> captureAc = getCaptureActions(actions, state, turn); //get list of capture actions
			if (!captureAc.isEmpty()) {return captureAc;}	
			return actions; //if Capture actions found, always return that list otherwise return non-capture actions
		}
		
		/*
		 * Generate capture actions of State by looking at each entry in the Actions list passed to it
		 * if it is a capture, add the index to list, for double capture add it to list twice
		 */
		public List<int[]> getCaptureActions(List<int[]> ac, char[][] state, char turn) {
			List<int[]> capAc = new ArrayList<int[]>();
			Iterator<int[]> it = ac.iterator();
			boolean isCap=false;
			while(it.hasNext()){
				int[] in1 = it.next();	//look at each action in actions list
				int[] in2 = it.next();
				char[][] statecopy= cloneArray(state); //check for capture, double capture
				boolean isDoubleCap=isDoubleCapture(statecopy,in1,in2,turn);
				if (!isDoubleCap) isCap= moveToken(statecopy,in1,in2,turn,'c',0);
				if(isCap) { //if it is a capture, add the index to list
					capAc.add(in1);
					capAc.add(in2);
				}
				if (isDoubleCap) { //if it is a double capture add it to list twice
					capAc.add(in1);
					capAc.add(in2);
					capAc.add(in1);
					capAc.add(in2);
				}
			}
			if(capAc.isEmpty()) {
				return ac;
			}
			return capAc;
		}
		
		/************************** GAME FUNCTIONS ************************************/
		
		/*
		 * given 2 indices, check if index1 to index2 is a valid move
		 * if yes, move piece at index1 to index 2, assign 'e' (empty) to index1 position on board
		 * 		call performCapture with captureType(pick), to see if any capture is possible and perform the capture
		 * 		if any capture was performed return true, otherwise false
		 * otherwise return false 
		 */
		public boolean moveToken(char[][] conf, int[] in1, int[] in2, char turn, char player, int pick){
			boolean capturedSomething=false;
			char[][] beforeMoving = cloneArray(conf); //saving board configuration to check if move is a capture or not
			if (isNull1x2(in1) || isNull1x2(in2)) return false;
			int[][] adjacency=refinedAdjacency(conf, in1);
			int dir=whichDirection(adjacency,in2);	//find the direction in which to perform capture, i.e. U,D,L,R,LU,LD,RU,RD
			if (dir==-1) return false;		//if direction is -1, no move possible, thus no capture
			conf[in2[0]][in2[1]]=turn;
			conf[in1[0]][in1[1]]='e';
			performCapture(conf,in1, in2,dir,turn,player, pick);  //if direction is valid send 2 indices to perform capture after moving from in1 to in2
			
			if(pieceCaptured(beforeMoving,conf)) { //if capture return true else return false
				capturedSomething=true;
			}
			return capturedSomething;
		}
		
		/*
		 * given 2 indices and a valid direction to move from index 1 to index2, call genericCapture for any detected valid captures
		 * check if there is a forward capture or a backward capture
		 * if both and player is human, ask which one to perform 
		 * 		for player Computer, alphabetaSearch will provide captureType 
		 */
		public void performCapture(char[][] conf,int[] in1, int[] in2,int dir,char turn, char player, int pick){
			char[][] confcopy=cloneArray(conf);
			genericCapture(confcopy,in2,dir,turn);
			boolean fwd=pieceCaptured(confcopy,conf); //check if fwd for approach capture, rev for reverse capture 
			int dircopy=flipDirection(dir);
			confcopy=cloneArray(conf);
			genericCapture(confcopy,in1,dircopy,turn);
			boolean rev=pieceCaptured(confcopy,conf);
			if(fwd && rev) {
				if (player =='h') {				//if both and player human, get choice about which capture to make
					Scanner sc= new Scanner(System.in);
					out.println("Choose Approach capture or Withdraw capture");
					out.println("Enter 1 for Approach Capture or -1 for Reverse Capture");
					while (!isValidPick(pick)){
						if (sc.hasNextInt()) {
							pick = sc.nextInt();
						}
					}
					if (pick ==1) genericCapture(conf,in2,dir,turn); //for double capture perform capture based on pick(CaptureType)
					else genericCapture(conf,in1,dir,turn);
				}	
			}
			else if(fwd) {genericCapture(conf,in2,dir,turn);} //for single capture, perform which ever is possible
			else if(rev) genericCapture(conf,in1,dircopy,turn);
		}
		
		/*
		 * given if direction from index1 to index 2 is valid, and opponent piece is at index 2, perform chained capture
		 * findNext with configuration and currentIndex finds the next possible capture
		 */
		public void genericCapture(char[][] conf,int[] in,int dir,char turn) {
			char opponent=flipTurn(turn); //flip turn to get opponent
			int[][] adj=createAdjacency(in); 
			int[] capIn=adj[dir]; //find first capture from adjacency(neighbors)
			int[] next=capIn;
			while (!isNull1x2(next) && !isNull1x2(in) && conf[next[0]][next[1]]==opponent) { 
				conf[next[0]][next[1]]='e';
				next=findNext(conf, next,dir,turn); //get next Index of opponent in the same direction
			}
		}
		
		/*
		 * for 5x5 at most 3 pieces can be captured at a time, 
		 * this function returns the index of the next piece of opponent to be captured
		 * using the direction of movement (U,D,L,R,LU,LD,RU,RD) 
		 */
		public int[] findNext(char[][] conf, int[] in, int dir, char turn){
			int[] nextNeighbor = in;
			char opponent=flipTurn(turn);
			int i=in[0];
			int j=in[1];
			switch(dir){ //based on which direction is entered generate the next index to be captured
			case 0: if(i-1>=0) {nextNeighbor = new int[] {i-1,j};} break;
			case 1: if(i+1<5) {nextNeighbor = new int[] {i+1,j};} break;
			case 2: if(j-1>=0) {nextNeighbor = new int[] {i,j-1};} break;
			case 3: if(j+1<5) {nextNeighbor = new int[] {i,j+1};} break;
			case 4: if(i-1>=0 && j-1>=0) {nextNeighbor = new int[] {i-1,j-1};} break;
			case 5: if(i+1<5 && j-1>=0) {nextNeighbor = new int[] {i+1,j-1};} break;
			case 6: if(i-1>=0 && j+1<5) {nextNeighbor = new int[] {i-1,j+1};} break;
			case 7: if(i+1<5 && j+1<5) {nextNeighbor = new int[] {i+1,j+1};} break;
			case -1: nextNeighbor = new int[] {-1,-1};
			}
			i=nextNeighbor[0];
			j=nextNeighbor[1];
			//if the piece at next index is not an opponent, return {-1,-1}
			if (!isNull1x2(nextNeighbor) && conf[i][j] != opponent) {nextNeighbor= new int[] {-1,-1};}
			//ERROR condition: if index entered and next neighbor index are the same, return {-1,-1} 
			if (Arrays.equals(nextNeighbor, in)) {nextNeighbor= new int[] {-1,-1};} 
			return nextNeighbor;
		}
		
		/*
		 * In the adjacency list(list of neighbors given) which position refers to 'in', 
		 * that position is the index
		 */
		public int whichDirection(int[][] adj,int[] in){
			int direction=-1;
			for(int i=0; i<8; ++i){
				if (Arrays.equals(adj[i],in)) direction=i;
			}
			return direction;
		}

		/*
		 * This is roughly my move generator
		 * Create a list of neighbors of in1 from the configuration of board
		 * if any of the neighbors are not empty, make those indices {-1,-1} 
		 */
		public int[][] refinedAdjacency(char[][] conf, int[] in1){
			int p,q;
			int[][] adj= createAdjacency(in1); //list of neighbor indices
			for(int i=0; i<8; ++i){
				if (!isNull1x2(adj[i])) {
					p=adj[i][0];
					q=adj[i][1];
					if (conf[p][q]!= 'e') {  //check if neighbor is not empty
						adj[i][0]=-1;		//make the index -1,-1
						adj[i][1]=-1;
					}
				}
			}
			return adj;
		}
		
		/*
		 * Creates a list of neighbors, from a list of legalMoves
		 * if a direction is a legal move, then the adjacency element at that position will be a neighbor index
		 */
		public int[][] createAdjacency(int[] in){
			int[] mo = legalMoves(in); //array of all directions a piece can move in
			int i=in[0];
			int j=in[1];
			int[] neighbor= {-1,-1};
			int[][] adj= {neighbor, neighbor, neighbor, neighbor, neighbor, neighbor, neighbor, neighbor};
			//if the array element of that direction is set, add the corresponding index to adjacency
			if (mo[0]==1) {adj[0] = new int[] {i-1,j};}
			if (mo[1]==1) {adj[1] = new int[] {i+1,j};}
			if (mo[2]==1) {adj[2] = new int[] {i,j-1};}
			if (mo[3]==1) {adj[3] = new int[] {i,j+1};}
			if (mo[4]==1) {adj[4] = new int[] {i-1,j-1};}
			if (mo[5]==1) {adj[5] = new int[] {i+1,j-1};}
			if (mo[6]==1) {adj[6] = new int[] {i-1,j+1};}
			if (mo[7]==1) {adj[7] = new int[] {i+1,j+1};}
			return adj;
		}
		
		/*
		 * returns an array of legal moves for index 'in'
		 * the element at position i, will be set if piece at 'in' can move in direction 'i'
		 * i goes from 0->7, corresponding directions are
		 * {Up, Down, Left, Right, Left-Up, Left-Down, Right-Up, Right-Down}
		 */
		public int[] legalMoves(int[] in){
			int i =in[0];
			int j = in[1];
			int[] mo= {0,0,0,0,0,0,0,0}; //initialize legal moves to array of zeros
			//if the piece can move in specified direction without going out out game bounds(i.e. <5, >=0) set that direction
			if (i-1 >=0) {mo[0]=1;} 
			if (i+1 <5) {mo[1]=1;}
			if (j-1 >=0) {mo[2]=1;}
			if (j+1 <5) {mo[3]=1;}
			if ((i==1 || i==3) && (j==1 || j==3)) {mo[4]=1; mo[5]=1; mo[6]=1; mo[7]=1;}
			if (i==2 && j==2) {mo[4]=1; mo[5]=1; mo[6]=1; mo[7]=1;}
			if ((i==2 || i==4) && (j==2 || j==4)) {mo[4] =1;}
			if ((i==0 || i==2) && (j==2 || j==4)) {mo[5] =1;}
			if ((i==2 || i==4) && (j==0 || j==2)) {mo[6] =1;}
			if ((i==0 || i==2) && (j==0 || j==2)) {mo[5] =1;}
			return mo;
		}
		
		/******************************* HELPER FUNCTIONS ***********************************/
		
		/*
		 * Checks if the move is a Double capture
		 */
		public boolean isDoubleCapture(char[][] conf,int[] in1,int[] in2,char turn){
			if (isNull1x2(in1) || isNull1x2(in2)) return false;
			int[][] adjacency=refinedAdjacency(conf, in1); //creates matrix of empty neighbors
			int dir=whichDirection(adjacency,in2); 
			if (dir==-1) return false;
			char[][] confcopy=cloneArray(conf);
			genericCapture(confcopy,in2,dir,turn);
			boolean fwd=pieceCaptured(confcopy,conf); //checks if Approach capture is possible
			int dircopy=flipDirection(dir);
			confcopy=cloneArray(conf);
			genericCapture(confcopy,in1,dircopy,turn);
			boolean rev=pieceCaptured(confcopy,conf); //checks if Withdraw capture is possible
			if(fwd && rev) return true;
			return false;
		}
		
		/*
		 * Given 2 board configurations checks for a count of 'e' (Empty positions)
		 * to determine if a capture was made
		 */
		public boolean pieceCaptured(char[][] before, char[][] after){
			int beforeE = tokenCtr(before,'e');
			int afterE = tokenCtr(after,'e');
			//out.println(beforeE+" "+afterE);
			if (beforeE!=afterE) {return true;} //counts unequal, means capture made return true
			else return false;
		}
		
		/*
		 * given a board configuration (state), and a character (whichChar)
		 * returns the count of whichChar
		 */
		public int tokenCtr(char[][] state, char whichChar){
			int ctr=0;
			for(int i =0; i<5; ++i){
				for(int j =0; j<5; ++j){
					if (state[i][j]==whichChar) {ctr+=1;}
				}
			}
			return ctr;
		}
		
		/*
		 * given a board configuration (state), and a character (token)
		 * returns a 25x2 array of all positions of that token, padded by {-1,-1}
		 */
		public int[][] posOfChar(char[][] conf, char token) {
			int ctr=-1;
			int[][] arrPos= new int[25][2];
			for(int i =0; i<5; ++i){
				for(int j =0; j<5; ++j){
					if (conf[i][j]==token) {
						ctr+=1;
						arrPos[ctr]= new int[] {i,j};
					}
				}
			}
			++ctr;
			while(ctr<25) {
				Arrays.fill(arrPos[ctr], -1); //end padding
				++ctr;
			}
			return arrPos;
		}
		
		/*
		 * Gives direction of reverse capture i.e.
		 * if a piece moves from in1 to in2 in UP direction, the capture will be made from in1 in DOWN direction
		 * this function provides that flip of axis for Withdraw Captures
		 */
		public int flipDirection(int pos){
			if (pos==-1) {return -1;}
			if(pos!=4 && pos!=7){
				if (pos%2==0) {pos+=1;}
				else if (pos%2==1) {pos-=1;}
			}
			else if (pos==4) {pos=7;}
			else if (pos==7) {pos=4;}
			return pos;
		}
		
		/*
		 * Next 2 functions are used after each gameplay turn to flip the turn from Black to White
		 * and flip the player from Human to Computer respectively 
		 */
		public char flipTurn(char turn) {
			if (turn=='b') {return 'w';}
			return 'b';
		}
		public char flipPlayer(char player) {
			if (player=='c') {return 'h';}
			return 'c';
		}
		
		/*
		 * Creates the initial Board configuration
		 */
		public void startConfig(char[][] conf) {
			out.println();
			for (int i=0; i<2; ++i){
				for (int j=0; j<5; ++j){
					conf[i][j] = 'b';
				}
			}
			
			for (int i=3; i<5; ++i){
				for (int j=0; j<5; ++j){
					conf[i][j] = 'w';
				}
			}

			conf[2][0]='b';
			conf[2][1]='w';
			conf[2][2]='e';
			conf[2][3]='b';
			conf[2][4]='w';
		}
		
		/*
		 * Prints board configuration passed as conf
		 */
		public void printConfig(char[][] conf){
			out.println();
			for (int i=0; i<5; ++i){
				for (int j=0; j<5; ++j){
					out.print(conf[i][j]+"("+i+","+j+")"+" ");
				}
				out.println();
			}
		}
		
		/*
		 * Essentially an OR of 2 boolean values to make code more readable
		 */
		public boolean anyCapture(boolean a, boolean b){
			if (a || b) return true;
			else return false;
		}
		
		/*
		 * For debugging purposes, prints out ArrayList of type int[] to console
		 */
		public void printActionList(List<int[]> l1){
			Iterator<int[]> it = l1.iterator();
			while(it.hasNext()){
				out.print(Arrays.toString(it.next())+" ");
				out.println(Arrays.toString(it.next()));
			}
		}
		
		/*
		 * used to check if any index is {-1,-1} as that can throw an exception at runtime
		 */
		public boolean isNull1x2(int[] arr){
			if (arr[0]==-1 || arr[1]==-1) return true;
			else return false;
		}
		
		/*
		 * Checks the integrity of user input for choosing CaptureType
		 */
		public boolean isValidPick(int pick) {
			if (pick==1) return true;
			if (pick ==-1) return true;
			else return false;
		}
		
		/*
		 * checks integrity of user input for choice of Black Player or While Player
		 */
		public char getChoice(char ch){
			Scanner sc = new Scanner(System.in);
			while (ch != 'b' && ch != 'w') {
				out.println("Enter 'w' for white player 'b' for black player");
				String s1 = sc.next();
				ch = s1.charAt(0);
			}
			return ch;
		}
		
		/*
		 * creates a clone of 2D array, for readability
		 */
		public char[][] cloneArray(char[][] src) {
		    int length = src.length;
		    char[][] target = new char[length][src[0].length];
		    for (int i = 0; i < length; i++) {
		        System.arraycopy(src[i], 0, target[i], 0, src[i].length);
		    }
		    return target;
		}
	
		/************************* CLASS FUNCTIONS TO ACCESS CLASS VARIABLES ******************/
		public int[][] getAction(){
			return action;
		}
		
		public int getPaika(){
			return paika;
		}
	
		public void putPaika(int p){
			paika=p;
		}
	
		public void putValue(int v){
			value=v;
		}
	
		public int getValue(){
			return value;
		}
	
		public void putAction(int[] in1,int[] in2){
			action[0]=in1;
			action[1]=in2;
		}
	
		public int getPick() {
			return pick;
		}
	
		public void putPick(int p) {
			pick=p;
		}
		
		public int getPruneMin(){
			return pruneMin;
		}
		
		public void incrementPruneMin(){
			++pruneMin;
		}
		
		public int getPruneMax(){
			return pruneMax;
		}
		
		public void incrementPruneMax(){
			++pruneMax;
		}
		
		public int getNodesGen(){
			return nodesGen;
		}
		
		public void incrementNodesGen(int p){
			nodesGen+=p;
		}
	}
