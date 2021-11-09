/**
 * This class implements the logic behind the BDD for the n-queens problem
 * You should implement all the missing methods
 */

import java.util.*;

import net.sf.javabdd.*;

public class QueensLogic {
    private int x = 0;
    private int y = 0;
    private int[][] board;


    private BDDFactory fact = JFactory.init(2000000, 200000);
    private BDD rules = fact.one();
    private int N = 8;

    public QueensLogic() {

    }

    public void initializeGame(int size) {
        this.x = size;
        this.y = size;
        this.board = new int[x][y];

        //Set variable numbers, which is the same as the chessfields
        //Else it crashes
        this.fact.setVarNum(N * N);

        generateRules();
    }

    /**
     * Method to generate rules
     */
    private void generateRules() {
        BDD queenRule = queenInRowsRule();
        this.rules = queenRule;

        this.rules = this.rules.and(createChessFieldRules());
    }

    /**
     * Method to generate the 'row' queen rule
     * described in http://configit.com/configit_wordpress/wp-content/uploads/2013/07/bdd-eap.pdf
     *
     * @return
     */
    public BDD queenInRowsRule() {
        BDD rowCombiner = fact.one();

        for (int i = 0; i < N; i++) {
            BDD rowRule = fact.zero();

            for (int j = 0; j < N; j++) {
                rowRule = or(rowRule, convertPosition(i, j));
            }

            //Foreach i, the statement must be true, therefor combining each row with an and statement,
            //Will secure each row has a queen
            rowCombiner = rowCombiner.and(rowRule);
        }

        return rowCombiner;
    }

    /**
     * Method to generate the rules beside the row rule,
     * described in http://configit.com/configit_wordpress/wp-content/uploads/2013/07/bdd-eap.pdf
     */
    private BDD createChessFieldRules() {

        BDD rtnRules = fact.one();

        // Loop through the board
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                // Generate the rules for the cell i,j
                // Instead of using 1 as the starting point as suggested in the paper
                // We make it with array indexes which starts on 0
                BDD rules = fact.one();
                for (int l = 0; l < N && j != l; l++)
                    rules = and(rules, convertPosition(i, l));

                for (int k = 0; k < N && i != k; k++)
                    rules = and(rules, convertPosition(k, j));

                for (int k = 0; k < N && k != i; k++)
                    if ((0 <= j + k - i) && (j + k - i < N)) //Fails, if this is not an if statement
                        rules = and(rules, convertPosition(k, j + k - i));

                for (int k = 0; k < N && i != k; k++)
                    if ((0 <= j + i - k) && (j + i - k < N)) //Fails, if this is not an if statement
                        rules = and(rules, convertPosition(k, i + j - k));

                BDD or = fact.zero().or(this.fact.nithVar(convertPosition(i, j)));
                or = or.or(rules);

                // Add the rules generated to the boards rules
                rtnRules = rtnRules.and(or);
            }
        }

        return rtnRules;
    }

    public boolean insertQueen(int column, int row) {

        if (board[column][row] == -1 || board[column][row] == 1) {
            return true;
        }

        board[column][row] = 1;

        // Add the current position to the BDD rules
        this.rules = this.rules.restrict(this.fact.ithVar(convertPosition(column, row)));

        //Loop through board
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                if (!placeable(i, j)) //If the BDD isnt compatible with placing a queen on i,j
                    board[i][j] = -1; //Then set it to -1 to symbolise a false solution

        return true;
    }

    /**
     * HELPERS
     */

    /**
     * Method to convert chess board positions into a integer value
     * Logic stolen from SO
     * http://stackoverflow.com/questions/14015556/how-to-map-the-indexes-of-a-matrix-to-a-1-dimensional-array-c
     */
    private int convertPosition(int i, int j) {
        return (j * this.N) + i;
    }

    /**
     * Shorthand for and logic
     *
     * @param bdd
     * @param var
     * @return
     */
    public BDD and(BDD bdd, int var) {
        return bdd.and(this.fact.nithVar(var));
    }

    /**
     * Shorthand for or logic
     *
     * @param bdd
     * @param var
     * @return
     */
    public BDD or(BDD bdd, int var) {
        return bdd.or(this.fact.ithVar(var));
    }

    /**
     * Method to check if a queen in i,j can be placed there
     *
     * @param i
     * @param j
     * @return
     */
    private boolean placeable(int i, int j) {
        return !this.rules.restrict(this.fact.ithVar(convertPosition(i, j))).isZero();
    }

    public int[][] getGameBoard() {
        return board;
    }
}
