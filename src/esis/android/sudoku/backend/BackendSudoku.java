package esis.android.sudoku.backend;

import java.util.Random;

import android.util.Log;

/**
 * @author Sergej Thomas
 * @author Sebastian Guillen
 */

public class BackendSudoku {

	public static final int SIZE = 9;
	private static final String TAG = BackendSudoku.class.getSimpleName();
	
	/** Maximale Anzahl Zahlen, die theoretisch entfernt werden koennten */
	private static final int REMOVE_LOTS = 63;
	private static final int REMOVE_SOME = 45;
	private static final int REMOVE_FEW = 35;
	
	/** < Raetselfeld */
	public int unsolved_grid[][];
	/** < Loesungsfeld */
	public int solved_grid[][];
	
	/** < Anzahl der moeglichen Loesungen */
	private int numberOfSolutions;
	/** < Zufallszahlen zum erzeugen vom Sudoku */
	private int random;
	/** < Erste zufaellige Koordinate zum Zahlen entfernen */
	private int random_coordinate;	
	/** < Sicherheitskopie der zu entfernenden Zahl */
	private int backup1;	
	/** < Sicherheitskopie der zu entfernenden drehsymmetrischen Zahl */
	private int backup2;
	/** < Anzahl zu pruefender Zellen im Feld */
	private int elem;	
	private Random rand;
	
	private class Zellen{		
		private int zeile;
		private int spalte;		
		public int getZeile(){return zeile;}
		public void setZeile(int x){zeile = x;}
		public int getSpalte(){return spalte;}
		private void setSpalte(int x){spalte = x;}//FIXME ?
	}
	
	Zellen[] pZellen;
	public int quantityRemoved = 0;

	public BackendSudoku() {

		unsolved_grid = new int[SIZE][SIZE];
		solved_grid = new int[SIZE][SIZE];
		pZellen = new Zellen[SIZE*SIZE];
		rand = new Random();

		numberOfSolutions = 0;
		random = 0;
		random_coordinate = 0;
		backup1 = 0;
		backup2 = 0;
		elem = 0;

		for (int row = 0; row < SIZE; ++row) {
			for (int column = 0; column < SIZE; ++column) {
				// Defaultinitialisierung des Loesungsfeldes
				solved_grid[row][column] = 0;
				// Defaultinitialisierung des Raetselfeldes
				unsolved_grid[row][column] = 0;
			}
		}
		for (int i = 0; i < SIZE*SIZE; ++i)
			pZellen[i] = new Zellen();
	}

	public void create_game(int difficulty) {
		// rand.nextInt();//srand(time(NULL));

		create_sudoku(0, 0, solved_grid);
		copyGrid();
		removeRndCells(difficulty);
	}

	private boolean create_sudoku(int zeile, int spalte, int[][] grid) {
		random = (1 + rand.nextInt(SIZE)); // Zufallszahl zwischen 1
		// und 9 erzeugen

		if(random < 1 || random > 9)
			System.out.println("scheisssssse!!! zufal gleich " + random);

		if (spalte > 8) // Zeilenwechsel
		{
			zeile++;
			spalte = 0;

			if (zeile > 8) // Abbruchbedingung
				return true; // Erfolgsmeldung, letze Zahl gepasst!
		}

		if (!check(zeile, spalte, random, grid)) // wenn Zufallszahl passt
		{
			grid[zeile][spalte] = random;

			if (create_sudoku(zeile, spalte + 1, grid)) // naechste Position
			// ueberpruefen
			{
				return true;// Erfolgsmeldung
			}
		} else // wenn Zufallszahl nicht passt, jede andere Zahl testen
		{
			for (int i = 1; i <= SIZE; i++) {
				if (!check(zeile, spalte, i, grid)) {
					grid[zeile][spalte] = i; // die erste, die passt nehmen
					if (create_sudoku(zeile, spalte + 1, grid)) // naechste Position ueberpruefen
						return true; // Erfolgsmeldung
				}
			}
		}
		grid[zeile][spalte] = 0; // keine Zahl gepasst BACKTRACKING!!!
		return false; // Misserfolg
	}

	private void removeRndCells(int difficulty) {//FIXME this is way to slow for REMOVE_LOTS

		// zu entfernende Zahlen
		int quantityToRemove = getHowManyNumbersToRemove(difficulty);

		for (int i = 0; i < SIZE; ++i)// belegte Zellen in feld speichern
			for (int j = 0; j < SIZE; ++j) {
				pZellen[elem].setZeile(i);
				pZellen[elem].setSpalte(j);
				++elem;
			}
		quantityRemoved = removeCells(quantityToRemove);
		Log.d(TAG, "Removed " + quantityRemoved + " numbers from " + quantityToRemove + " planned.");
	}

	private int removeCells(int quantityToRemove) {
	    while (countEmptyCells() < quantityToRemove) {
	    	if (elem <= 0)// abbrechen wenn keine moeglichkeiten mehr zur verfuegung stehen
	    	    break;
	    	random_coordinate = (rand.nextInt(elem));

	    	backup1 = unsolved_grid[pZellen[random_coordinate].zeile][pZellen[random_coordinate].spalte];
	    	backup2 = unsolved_grid[(SIZE - 1) - pZellen[random_coordinate].zeile][(SIZE - 1) - pZellen[random_coordinate].spalte];

	    	unsolved_grid[pZellen[random_coordinate].zeile][pZellen[random_coordinate].spalte] = 0;
	    	unsolved_grid[(SIZE - 1) - pZellen[random_coordinate].zeile][(SIZE - 1) - pZellen[random_coordinate].spalte] = 0;

	    	// Wenn nicht eindeutig loesbar, zahlen wiederherstellen
	    	if (!checkUnique(unsolved_grid)) {
	    		unsolved_grid[pZellen[random_coordinate].zeile][pZellen[random_coordinate].spalte] = backup1;
	    		unsolved_grid[(SIZE - 1) - pZellen[random_coordinate].zeile][(SIZE - 1)	- pZellen[random_coordinate].spalte] = backup2;
	    	}

	    	// Gepruefte Zellen aus pZellen-Feld entfernen			
	    	for (int i = random_coordinate; i < elem-1; ++i)//elem-1 sonst krachs
	    		pZellen[i] = pZellen[i + 1];
	    	elem--;
	    	if (pZellen[elem - random_coordinate].zeile != 4 && pZellen[elem - random_coordinate].spalte != 4) {
	    		for (int i = (elem - random_coordinate); i < elem; ++i)
	    			pZellen[i] = pZellen[i + 1];
	    		elem--;
	    	}
	    }
	    return countEmptyCells();
	}

	private static int getHowManyNumbersToRemove(int difficulty) {
		int quantityToRemove = 0;
		if (difficulty == MyApp.EASY)
		    quantityToRemove = REMOVE_FEW;
		else if (difficulty == MyApp.MEDIUM)
		    quantityToRemove = REMOVE_SOME;
		else if (difficulty == MyApp.HARD)
		    quantityToRemove = REMOVE_LOTS;
		return quantityToRemove;
	}

	private int countEmptyCells() {
		int emptyCells = 0;
		for (int i = 0; i < SIZE; i++)
			for (int j = 0; j < SIZE; j++)
				if (unsolved_grid[i][j] == 0)
					emptyCells++;
		return emptyCells;
	}

	private void copyGrid() {

		for (int i = 0; i < SIZE; ++i) {
			for (int j = 0; j < SIZE; ++j) {
				unsolved_grid[i][j] = solved_grid[i][j];
			}
		}
	}

	private boolean checkUnique(int[][] grid) {

		numberOfSolutions = 0;
		countSolutions(0, 0, grid);

		// loesungen == 0 sollte nie vorkommen

		if (numberOfSolutions == 1)
			return true;
		else
			return false;
	}

	private boolean countSolutions(int zeile, int spalte, int[][] grid) {

		// Zeilenwechsel
		if (spalte == SIZE) {
			zeile++;
			spalte = 0;

			// Abbruchbedingung
			if (zeile == SIZE)
			    return true; // Erfolgsmeldung, letze Zahl gepasst!
		}

		if (grid[zeile][spalte] > 0) // Zelle schon gesetzt
			return countSolutions(zeile, spalte + 1, grid); // eine Zelle weiter

		for (int i = 1; i <= SIZE; i++) {
			if (!check(zeile, spalte, i, grid)) {
				grid[zeile][spalte] = i; // passende Zahl setzen
				if (countSolutions(zeile, spalte + 1, grid)) {
					numberOfSolutions++; // Anzahl Loesungen hochzaehlen
				}
			}
		}

		grid[zeile][spalte] = 0; // keine Zahl gepasst BACKTRACKING!!!
		return false;

	}

	private boolean check(int zeile, int spalte, int zahl, int[][] grid) {
		if (checkZeile(zeile, zahl, grid) || checkSpalte(spalte, zahl, grid)
				|| checkBlock(zeile, spalte, zahl, grid))
			return true;// Es gibt schon die Zahl (Sudoku Regelverletzung)
		return false;
	}

	private boolean checkZeile(int zeile, int zahl, int[][] grid) {

		for (int i = 0; i < SIZE; ++i)
			if (grid[zeile][i] == zahl)
				return true;
		return false;
	}

	private boolean checkSpalte(int spalte, int zahl, int[][] grid) {

		for (int i = 0; i < SIZE; i++)
			if (grid[i][spalte] == zahl)
				return true;
		return false;
	}

	private boolean checkBlock(int zeile, int spalte, int zahl, int[][] grid) {

		// Linke obere Ecke(Anfang) des 3x3 Blocks bestimmen
		int s_start = (int) (spalte / 3) * 3;
		int z_start = (int) (zeile / 3) * 3;

		for (int i = z_start; i < z_start + 3; i++)
			for (int j = s_start; j < s_start + 3; j++)
				if (grid[i][j] == zahl)
					return true;
		return false;
	}

}
