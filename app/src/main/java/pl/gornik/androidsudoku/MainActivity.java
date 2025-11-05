package pl.gornik.androidsudoku;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    SoundPool soundPool;
    int confettiSoundId;
    private int selectedNumber = 1;
    private boolean[][] allowChangingNumber = new boolean[9][9];

    private int visibleSquares = 38;
    private int secondsElapsed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
        restart();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        confettiSoundId = soundPool.load(this, R.raw.confetti,1);
    }

    private void updateDisplayBoardColors(Integer[][] board, int[] colorsIDs) {
        GridLayout sudokuGrid = findViewById(R.id.sudokuGrid);
        int childIndex;
        int[] countNumbers = new int[10];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                countNumbers[board[i][j]]++;
            }
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                childIndex = i * 9 + j;
                TextView sudokuCellText = ((TextView)
                        ((LinearLayout) ((LinearLayout) sudokuGrid.getChildAt(childIndex))
                                .getChildAt(0)).getChildAt(0));
                if (String.valueOf(sudokuCellText.getText()).equalsIgnoreCase(String.valueOf(selectedNumber))) {
                    sudokuCellText.setBackgroundColor(getColor(R.color.selectedNumber));
                    if (countNumbers[Integer.valueOf((String) sudokuCellText.getText())] == 9 && isValid(board, i, j, board[i][j])) {
                        sudokuCellText.setBackgroundColor(colorsIDs[Integer.valueOf((String) sudokuCellText.getText())]);
                    }
                } else {
                    sudokuCellText.setBackgroundColor(0);
                }
                if (!sudokuCellText.getText().isEmpty()) {
                    if (!isValid(board, i, j, board[i][j])) {
                        sudokuCellText.setBackgroundColor(getColor(R.color.red));
                    }
                }

            }
        }
    }

    private void generateDisplayBoard(Integer[][] board, int[] tileColorIDs) {
        GridLayout sudokuGrid = findViewById(R.id.sudokuGrid);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                LinearLayout sudokuCellVertical = new LinearLayout(this);
                LinearLayout sudokuCellHorizontal = new LinearLayout(this);
                sudokuCellVertical.addView(sudokuCellHorizontal);
                sudokuCellVertical.setOrientation(LinearLayout.VERTICAL);
                sudokuCellHorizontal.setOrientation(LinearLayout.HORIZONTAL);
                TextView sudokuCellText = new TextView(this);
                sudokuCellText.setTextSize(30);
                sudokuCellText.setTextColor(getColor(R.color.white));
                int finalI = i;
                int finalJ = j;
                if (allowChangingNumber[finalI][finalJ]) {
                    sudokuCellText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (board[finalI][finalJ] == selectedNumber) {
                                sudokuCellText.setText("");
                                board[finalI][finalJ] = 0;
                            } else {
                                sudokuCellText.setText(String.valueOf(selectedNumber));
                                board[finalI][finalJ] = selectedNumber;
                            }
                            updateDisplayBoardColors(board, tileColorIDs);
                            if (isSolvedBoard(board)) {
                                //todo win
                                int childIndex;
                                for (int i = 0; i < 9; i++) {
                                    for (int j = 0; j < 9; j++) {
                                        childIndex = i * 9 + j;
                                        TextView sudokuCellText = ((TextView)
                                                ((LinearLayout) ((LinearLayout) sudokuGrid.getChildAt(childIndex))
                                                        .getChildAt(0)).getChildAt(0));
                                        sudokuCellText.setBackgroundColor(tileColorIDs[Integer.valueOf((String) sudokuCellText.getText())]);
                                        sudokuCellText.setOnClickListener(null);
                                    }
                                }
                                soundPool.play(confettiSoundId,1,1,0,0,1);
                            }
                        }
                    });
                } else {
                    sudokuCellText.setShadowLayer(10, 3, 3, getColor(R.color.black));
                }
                sudokuCellText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                sudokuCellText.setGravity(Gravity.CENTER);
                sudokuCellHorizontal.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 49));
                sudokuCellText.setLayoutParams(
                        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 49));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f); // 1 row, equal weight
                params.columnSpec = GridLayout.spec(j, 1f); // 1 column, equal weight
                params.width = 0;
                params.height = 0;
                sudokuCellVertical.setLayoutParams(params);
                sudokuCellText.setText(String.valueOf(board[i][j]));
                if (board[i][j] == 0) {
                    sudokuCellText.setText("");
                }
                sudokuCellHorizontal.addView(sudokuCellText);
                if ((j + 1) % 3 == 0 && j + 1 < 9) {
                    View vertLine = new View(this);
                    vertLine.setBackgroundColor(getColor(R.color.schemeDarkGreen));
                    LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    vertLine.setLayoutParams(lineParams);
                    sudokuCellHorizontal.addView(vertLine);
                }
                if ((i + 1) % 3 == 0 && i + 1 < 9) {
                    View horLine = new View(this);
                    horLine.setBackgroundColor(getColor(R.color.schemeDarkGreen));
                    LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
                    horLine.setLayoutParams(lineParams);
                    sudokuCellVertical.addView(horLine);
                }
                sudokuGrid.addView(sudokuCellVertical);
            }
        }
    }

    private void scriptButtons(Integer[][] board, int[] tileColorIDs) {
        LinearLayout layoutSudokuNumbersContainer = findViewById(R.id.numberBtnLayout);
        for (int i = 0; i < layoutSudokuNumbersContainer.getChildCount(); i++) {
            View child = layoutSudokuNumbersContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView numberView = (TextView) child;
                int finalI = i;
                numberView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedNumber = finalI + 1;
                        updateSelectedNumberDisplay();
                        updateDisplayBoardColors(board, tileColorIDs);

                    }
                });
            }

        }
        Button restartBtn = findViewById(R.id.btnRestart);
        Button highscoresBtn = findViewById(R.id.btnHighscores);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext()).setCancelable(true).setMessage("All your progress will be gone").
                        setTitle("Restart?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GridLayout sudokuGrid = findViewById(R.id.sudokuGrid);
                                sudokuGrid.removeViews(0,81);
                                restart();
                            }
                        }).show();
            }
        });
        //todo highscores
        Button btnDiffEasy = findViewById(R.id.btnDiffEasy);
        Button btnDiffMedium = findViewById(R.id.btnDiffMedium);
        Button btnDiffHard = findViewById(R.id.btnDiffHard);
        btnDiffEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleSquares=38;
                TextView textSelected = findViewById(R.id.selectedText);
                String difficulty = "";
                switch (visibleSquares){
                    case 38:
                        difficulty="EASY";
                        break;
                    case 32:
                        difficulty="MEDIUM";
                        break;
                    case 26:
                        difficulty="HARD";
                        break;
                }
                textSelected.setText("Selected:\n"+difficulty);
            }
        });
        btnDiffMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleSquares=32;
                TextView textSelected = findViewById(R.id.selectedText);
                String difficulty = "";
                switch (visibleSquares){
                    case 38:
                        difficulty="EASY";
                        break;
                    case 32:
                        difficulty="MEDIUM";
                        break;
                    case 26:
                        difficulty="HARD";
                        break;
                }
                textSelected.setText("Selected:\n"+difficulty);
            }
        });
        btnDiffHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleSquares=26;
                TextView textSelected = findViewById(R.id.selectedText);
                String difficulty = "";
                switch (visibleSquares){
                    case 38:
                        difficulty="EASY";
                        break;
                    case 32:
                        difficulty="MEDIUM";
                        break;
                    case 26:
                        difficulty="HARD";
                        break;
                }
                textSelected.setText("Selected:\n"+difficulty);
            }
        });
    }

    private void restart(){
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                allowChangingNumber[i][j]=false;
            }
        }
        TextView textPlaying = findViewById(R.id.playingText);
        TextView textSelected = findViewById(R.id.selectedText);
        String difficulty = "";
        switch (visibleSquares){
            case 38:
                difficulty="EASY";
                break;
            case 32:
                difficulty="MEDIUM";
                break;
            case 26:
                difficulty="HARD";
                break;
        }
        textSelected.setText("Selected:\n"+difficulty);
        textPlaying.setText("Playing:\n"+difficulty);
        Integer[][] numbersBoard = obscureBoard(scrambleBoard(generateSudokuBoard()), visibleSquares);
        int[] tileColorIDs = new int[]{getColor(R.color.black), getColor(R.color.sudokuOne),
                getColor(R.color.sudokuTwo), getColor(R.color.sudokuThree), getColor(R.color.sudokuFour),
                getColor(R.color.sudokuFive), getColor(R.color.sudokuSix), getColor(R.color.sudokuSeven),
                getColor(R.color.sudokuEight), getColor(R.color.sudokuNine)};
        scriptButtons(numbersBoard, tileColorIDs);
        generateDisplayBoard(numbersBoard, tileColorIDs);
        updateSelectedNumberDisplay();
        updateDisplayBoardColors(numbersBoard, tileColorIDs);
    }
    private void updateSelectedNumberDisplay() {
        LinearLayout layout = findViewById(R.id.numberBtnLayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) {
                TextView numberView = (TextView) child;
                if (i + 1 == selectedNumber) {
                    numberView.setTextSize(35);
                } else {
                    numberView.setTextSize(25);
                }
            }
        }

    }

    private Integer[][] generateSudokuBoard() {
        Integer[][] numbersBoard = new Integer[9][9];
        int rowCount = 0;
        int tripleRowCount = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 1; j <= 9; j++) {
                numbersBoard[i][j - 1] = (j + tripleRowCount) + i * 3;
                if (numbersBoard[i][j - 1] >= 10) {
                    if (tripleRowCount == 0) numbersBoard[i][j - 1] = numbersBoard[i][j - 1] - 9;
                    else numbersBoard[i][j - 1] = numbersBoard[i][j - 1] - (9 * tripleRowCount);
                    if (numbersBoard[i][j - 1] >= 10) {
                        numbersBoard[i][j - 1] = numbersBoard[i][j - 1] - 9;
                    }
                }
            }
            rowCount++;
            if (rowCount % 3 == 0) tripleRowCount++;
        }

        return numbersBoard;
    }

    @SuppressLint({"NewApi"})
    private Integer[][] scrambleBoard(Integer[][] board) {
        Integer[][] scrambledBoard = new Integer[9][9];
        Random random = new Random();
        //swapping rows within bands
        Integer[][] bandRows = new Integer[3][9];
        for (int i = 0; i < 3; i++) { //band loop
            for (int j = 0; j < 3; j++) { // row loop
                bandRows[j] = board[j + (3 * i)];
            }
            int loopLength = random.nextInt(15, 36);
            for (int j = 0; j < loopLength; j++) {
                Integer[] tempRow;
                int startRowIndex = random.nextInt(0, 3);
                int endRowIndex = random.nextInt(0, 3);
                while (startRowIndex == endRowIndex) endRowIndex = random.nextInt(0, 3);
                tempRow = bandRows[startRowIndex];
                bandRows[startRowIndex] = bandRows[endRowIndex];
                bandRows[endRowIndex] = tempRow;
            }
            for (int j = 0; j < 3; j++) {
                scrambledBoard[j + (3 * i)] = bandRows[j];
            }
        }
        //swapping cols within stacks
        Integer[][] stackCols = new Integer[3][9];
        for (int i = 0; i < 3; i++) { //stack loop
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    stackCols[j][k] = scrambledBoard[k][j + (i * 3)];
                }
            }
            int loopLength = random.nextInt(15, 36);
            for (int j = 0; j < loopLength; j++) {
                Integer[] tempCol;
                int startColIndex = random.nextInt(0, 3);
                int endColIndex = random.nextInt(0, 3);
                while (startColIndex == endColIndex) endColIndex = random.nextInt(0, 3);
                tempCol = stackCols[startColIndex];
                stackCols[startColIndex] = stackCols[endColIndex];
                stackCols[endColIndex] = tempCol;
            }

            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    scrambledBoard[k][j + (i * 3)] = stackCols[j][k];
                }
            }
        }
        //scramble bands
        Integer[][][] bands = new Integer[3][3][9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    bands[i][j][k] = scrambledBoard[(i * 3) + j][k];
                }
            }
        }
        int loopLength = random.nextInt(15, 36);
        for (int j = 0; j < loopLength; j++) {
            Integer[][] tempBand;
            int startBandIndex = random.nextInt(0, 3);
            int endBandIndex = random.nextInt(0, 3);
            while (startBandIndex == endBandIndex) endBandIndex = random.nextInt(0, 3);
            tempBand = bands[startBandIndex];
            bands[startBandIndex] = bands[endBandIndex];
            bands[endBandIndex] = tempBand;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    scrambledBoard[(i * 3) + j][k] = bands[i][j][k];
                }
            }
        }
        //scramble stacks
        Integer[][][] stacks = new Integer[3][3][9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    stacks[i][j][k] = scrambledBoard[k][j + (i * 3)];
                }
            }
        }
        loopLength = random.nextInt(15, 36);
        for (int j = 0; j < loopLength; j++) {
            Integer[][] tempStack;
            int startStackIndex = random.nextInt(0, 3);
            int endStackIndex = random.nextInt(0, 3);
            while (startStackIndex == endStackIndex) endStackIndex = random.nextInt(0, 3);
            tempStack = stacks[startStackIndex];
            stacks[startStackIndex] = stacks[endStackIndex];
            stacks[endStackIndex] = tempStack;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    scrambledBoard[k][j + (i * 3)] = stacks[i][j][k];
                }
            }
        }
        //relabel numbers
        Map<Integer, Integer> numberSubstiution = new HashMap<>();
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(numbers);
        for (int i = 1; i <= 9; i++) {
            numberSubstiution.put(i, numbers.get(i - 1));
        }
        Integer[][] tempBoard = new Integer[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                tempBoard[i][j] = numberSubstiution.get(scrambledBoard[i][j]);
            }
        }
        scrambledBoard = tempBoard;
        return scrambledBoard;
    }

    private Integer[][] obscureBoard(Integer[][] board, int visibleSquares) {
        Integer[][] obscuredBoard = new Integer[9][9];
        List<Integer[]> coordinatesRowColList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                coordinatesRowColList.add(new Integer[]{Integer.valueOf(i), Integer.valueOf(j)});
                obscuredBoard[i][j] = board[i][j];
            }
        }
        Collections.shuffle(coordinatesRowColList);
        int tempNumber;
        int numbersToRemove = 81 - visibleSquares;
        for (Integer[] coordinates : coordinatesRowColList) {
            tempNumber = obscuredBoard[coordinates[0]][coordinates[1]];
            obscuredBoard[coordinates[0]][coordinates[1]] = 0;
            Integer[][] boardCopyForSolve = new Integer[9][9];
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    boardCopyForSolve[i][j] = obscuredBoard[i][j];
                }
            }
            if (!canBoardBeSolved(boardCopyForSolve)) {
                obscuredBoard[coordinates[0]][coordinates[1]] = tempNumber;
            } else {
                allowChangingNumber[coordinates[0]][coordinates[1]] = true;
                numbersToRemove--;
            }
            if (numbersToRemove == 0) break;
        }
        return obscuredBoard;

    }

    private boolean canBoardBeSolved(Integer[][] board) {
        Integer[] emptyCellCoordinatesRowCol = new Integer[]{10, 10};
        outer:
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    emptyCellCoordinatesRowCol[0] = i;
                    emptyCellCoordinatesRowCol[1] = j;
                    break outer;
                }
            }
        }
        if (emptyCellCoordinatesRowCol[0] == 10) return true;
        for (int i = 1; i <= 9; i++) {
            if (isValid(board, emptyCellCoordinatesRowCol[0], emptyCellCoordinatesRowCol[1], i)) {
                board[emptyCellCoordinatesRowCol[0]][emptyCellCoordinatesRowCol[1]] = i;
                if (canBoardBeSolved(board)) return true;
                board[emptyCellCoordinatesRowCol[0]][emptyCellCoordinatesRowCol[1]] = 0;
            }
        }
        return false;
    }

    private boolean isSolvedBoard(Integer[][] board) {
        //check rows
        Set<Integer> numsInRow = new HashSet<>();
        Set<Integer> numsInCols = new HashSet<>();
        Set<Integer> numsInSquare = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            numsInRow.addAll(Arrays.asList(board[i]));
            if (numsInRow.size() < 9 || numsInRow.contains(0)) return false;
        }
        //check cols
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                numsInCols.add(board[j][i]);
            }
            if (numsInCols.size() < 9 || numsInCols.contains(0)) return false;
            numsInCols.clear();
        }
        //check squares
        for (int l = 0; l < 3; l++) {
            for (int k = 0; k < 3; k++) {
                for (int i = 0; i < 3; i++) { //iter through 3 rows
                    for (int j = 0; j < 3; j++) { //iter through 3 cols
                        numsInSquare.add(board[i + (k * 3)][j + (l * 3)]);
                    }
                }
                if (numsInSquare.size() < 9 || numsInSquare.contains(0)) return false;
                numsInSquare.clear();
            }
        }
        return true;
    }

    private boolean isValid(Integer[][] board, int row, int col, int number) {
        //check row
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == number && i != col) return false;
        }
        //check col
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == number && i != row) return false;
        }
        //check square
        for (int i = 0; i < 3; i++) { //iter through 3 rows
            for (int j = 0; j < 3; j++) { //iter through 3 cols
                if (board[i + ((row / 3) * 3)][j + ((col / 3) * 3)] == number &&
                        i + ((row / 3) * 3) != row && j + ((col / 3) * 3) != col)
                    return false;
            }
        }
        return true;
    }
}