package UDP;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.net.*;

/*
 * Battleship_1.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */

/**
 * Second player of the battleship game
 * 
 * @author Lahari Chepuri
 * @author Smita Subhadarshinee Mishra
 */

public class Battleship_2 {
	DatagramPacket readFrom;
	DatagramPacket writeTo;
	DatagramSocket soc;

	String ocean;
	String hostName;
	int curr_port;
	int connect_to_port;
	boolean first;
	boolean second;

	String WIDTH = "width";
	String HEIGHT = "height";
	String WATER = "w";
	int WATER_VALUE = -1;
	int WATER_HIT_VALUE = -2;
	int HIT_VALUE = -3;
	String lineDelimiter = "#";

	int[][] battleField = null; // This is the field shown to the player
	int[][] originalBattleField = null; // will be initialized once
	String battleFieldString = null; // will be initialized once
	int battleFieldWidth = 0;
	int battleFieldHeight = 0;
	Scanner battleFieldParser = null;
	char hit = 'x';
	String fileName = null;

	void parseArgs(String[] args) {
		this.ocean = args[0];
		this.hostName = "localhost";
		this.first = true;
		this.second = false;
		if (args.length > 1) {
			curr_port = Integer.parseInt(args[1]);
			connect_to_port = Integer.parseInt(args[2]);
		} else {
			curr_port = 9993;
			connect_to_port = 9989;
		}
	}

	private void removeAllPartsOfBoat(int column, int row) {
		int boatId = originalBattleField[column][row];
		for (int rows = 0; rows < battleFieldHeight; rows++) {
			for (int columns = 0; columns < battleFieldWidth; columns++) {
				if (originalBattleField[columns][rows] == boatId)
					battleField[columns][rows] = HIT_VALUE;
			}
		}
	}

	private void printBattleField(int[][] battleField) {
		System.out.println();
		for (int rows = 0; rows < battleFieldHeight; rows++) {
			for (int columns = 0; columns < battleFieldWidth; columns++) {
				if (battleField[columns][rows] == WATER_VALUE)
					System.out.print("w" + " ");
				else
					System.out.print(battleField[columns][rows] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	private void printBattleFieldForPlayer(int[][] battleField) {
		System.out.println();
		System.out.println("x indicates a hit.");
		System.out.println("w indicates a miss, but you know now there is water.");
		System.out.println(". indicates boat oder water.\n");
		System.out.print("   ");
		for (int columns = 0; columns < battleFieldWidth; columns++) {
			System.out.print(" " + columns);
		}
		System.out.println(" ---> columns");
		for (int rows = 0; rows < battleFieldHeight; rows++) {
			System.out.print(rows + ": ");
			for (int columns = 0; columns < battleFieldWidth; columns++) {
				if (battleField[columns][rows] == WATER_HIT_VALUE)
					System.out.print(" " + "w");
				else if (battleField[columns][rows] == HIT_VALUE)
					System.out.print(" " + "x");
				else
					System.out.print(" " + ".");
			}
			System.out.println();
		}
		System.out.println();
	}

	private boolean isThereAboatLeft() {
		boolean rValue = false;
		for (int rows = 0; !rValue && rows < battleFieldHeight; rows++) {
			for (int columns = 0; !rValue && columns < battleFieldWidth; columns++) {
				if (battleField[columns][rows] >= 0)
					rValue = true;
			}
		}
		return rValue;
	}

	private boolean allWater() {
		for (int column = 0; column < battleFieldWidth; column++) {
			for (int row = 0; row < battleFieldWidth; row++) {
				if (battleField[column][row] != WATER_VALUE)
					return false;
			}
		}
		return true;
	}

	private void readHeightWidth() {
		for (int index = 0; index < 2; index++) {
			if (battleFieldParser.hasNextLine()) {
				String[] oneDimension = battleFieldParser.nextLine().split("\\s+");
				if (oneDimension[0].equals(WIDTH))
					battleFieldWidth = Integer.parseInt(oneDimension[1]);
				else
					battleFieldHeight = Integer.parseInt(oneDimension[1]);
			}
		}
	}

	private void createBattleField() {
		battleField = new int[battleFieldWidth][battleFieldHeight];
		originalBattleField = new int[battleFieldWidth][battleFieldHeight];
		for (int columns = 0; columns < battleFieldWidth; columns++) {
			for (int rows = 0; rows < battleFieldHeight; rows++) {
				battleField[columns][rows] = WATER_VALUE;
				originalBattleField[columns][rows] = WATER_VALUE;
			}
		}
	}

	private void updateBattleField() {
		for (int columns = 0; columns < battleFieldWidth; columns++) {
			for (int rows = 0; rows < battleFieldHeight; rows++) {
				if (originalBattleField[columns][rows] >= 0)
					battleField[columns][rows] = 0;
			}
		}
	}

	private void readBattleFieldScenario() {
		for (int index = 0; index < battleFieldHeight; index++) {
			if (battleFieldParser.hasNextLine()) {
				String[] oneRow = battleFieldParser.nextLine().split("\\s+");
				for (int xPosition = 1; xPosition < battleFieldWidth + 1; xPosition++) {
					if (!oneRow[xPosition].equals(WATER)) {
						String id = oneRow[xPosition].substring(1, oneRow[xPosition].length());
						originalBattleField[xPosition - 1][index] = Integer.parseInt(id);
					}
				}
			}
		}
	}

	private void readBattleFieldFile(String fileName) { // new
		if (fileName.equals("exit")) {
			System.exit(0);
		}
		int column = 0;
		try {
			battleFieldString = "";
			battleFieldParser = new Scanner(new File(fileName));
			while (battleFieldParser.hasNextLine()) {
				battleFieldString += battleFieldParser.nextLine() + lineDelimiter;
			}

		} catch (FileNotFoundException e) {
			System.out.println("Can't find that file! Try Again.");
		}

	}

	// new
	private void readBattleFieldFromString(String theBattleFieldInStringForm) {

		int column = 0;
		theBattleFieldInStringForm = theBattleFieldInStringForm.replaceAll(lineDelimiter, "\n");
		battleFieldParser = new Scanner(theBattleFieldInStringForm);
		readHeightWidth();
		createBattleField();
		readBattleFieldScenario();
		updateBattleField();

	}

	private int readOneIntValue(Scanner readUserInput, String text) {
		System.out.print(text);
		if (readUserInput.hasNextInt())
			return readUserInput.nextInt();
		else {
			System.out.println("Can't read next integer - RIP");
			System.exit(1);
		}
		return -1;
	}

	private boolean checkRange(int minValue, int value, int maxValue, String errorMessage) {
		if ((minValue <= value) && (value < maxValue)) {
			return true;
		} else {
			System.out.println("Error: " + errorMessage);
			return false;
		}
	}

	/**
	 * reads the state of ocean
	 */
	
	private void readTheOceans() { // new
		if (first) {
			sendData(battleFieldString);

			String theOtherOnes = readData();
			readBattleFieldFromString(theOtherOnes);
		} else {
			String theOtherOnes = readData();
			readBattleFieldFromString(theOtherOnes);

			sendData(battleFieldString);
		}
	}
	
	/**
	 * Plays the game and prints the state of ocean
	 */

	private void play() {
		readTheOceans(); // new
		Scanner readUserInput = new Scanner(System.in);
		int row = 0;
		int column = 0;
		int soManyTries = 0;
		while (isThereAboatLeft()) {
			soManyTries++;
			printBattleFieldForPlayer(battleField);
			column = readOneIntValue(readUserInput, "column coordinate (0 <= column < " + battleFieldWidth + "): ");
			row = readOneIntValue(readUserInput, "row	coordinate (0 <= row	< " + battleFieldHeight + "): ");
			if (checkRange(0, column, battleFieldWidth, "Column out of range. " + column)
					&& checkRange(0, row, battleFieldHeight, "Row out of range. " + row))
				if (originalBattleField[column][row] == WATER_VALUE) {
					battleField[column][row] = WATER_HIT_VALUE;
				} else {
					System.out.println("HIT");
					removeAllPartsOfBoat(column, row);
				}
			printBattleFieldForPlayer(battleField);
		}
		printBattleFieldForPlayer(battleField);
		soc.close();
	}

	/**
	 * Sends the current ocean to the other player
	 * or another datagram socket
	 */
	
	private void sendData(String theData) { // new
		System.out.println("sendData: ");
		try {
			byte[] buf = new byte[1024];
			InetAddress ip = InetAddress.getByName(hostName);
			writeTo = new DatagramPacket(theData.getBytes(), theData.getBytes().length, ip, connect_to_port);
			soc.send(writeTo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the ocean from another datagram socket
	 * 
	 * @return the value read from another socket
	 */
	
	private String readData() { // new
		System.out.println("readData: ");
		String rValue = "";
		try {
			byte[] get_data = new byte[1024];

			readFrom = new DatagramPacket(get_data, get_data.length);
			soc.receive(readFrom);
			rValue += new String(readFrom.getData()) + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rValue;
	}

	/**
	 * creates a new datagram socket
	 */
	
	private void setUpIO() { // new
		try {
			soc = new DatagramSocket(curr_port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * starts the game
	 * 
	 * @param args command line arguments
	 */

	private void playTheGame(String[] args) {
		parseArgs(args);
		readBattleFieldFile(ocean); // new
		setUpIO();
		play();
	}
	
	/**
	 * The main program.
	 *
	 * @param args command line arguments (filename, currentPlayerPort, otherPlayerPort)
	 */
	
	public static void main(String[] args) {
		new Battleship_2().playTheGame(args);
	}
}