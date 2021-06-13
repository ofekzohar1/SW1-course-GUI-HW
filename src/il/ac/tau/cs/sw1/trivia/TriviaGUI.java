package il.ac.tau.cs.sw1.trivia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TriviaGUI {

    private static final int MAX_ERRORS = 3;
    private Shell shell;
    private Label scoreLabel;
    private Composite questionPanel;
    private Label startupMessageLabel;
    private Font boldFont;
    private LastAnswer lastAnswer;

    // Game attributes
    private Set<Helper> isFirstHelper;
    private int score;
    private int numberOfMistakes;
    private List<String[]> questionsAndAnswers;
    private String correctAnswer;
    private int currQuestionIndex;
    private int numOfPassedQuestions;
    private boolean fiftyInAction;
    private boolean isGameEnded;
    private static final String SEP = "\t";
    private final Random rand = new Random();
    private static final int TOT_QUESTION_ANSWERS_LENGTH = 5; // 1 question + 4 answers
    private static final int CORRECT_ANSWER_INDEX = 1;
    private static final int QUESTION_INDEX = 0;
    private static final int HELPER_SCORE_COST = -1;


    // Currently visible UI elements.
    Label instructionLabel;
    Label questionLabel;
    private List<Button> answerButtons = new LinkedList<>();
    private Button passButton;
    private Button fiftyFiftyButton;

    public void open() {
        createShell();
        runApplication();
    }

    /**
     * Creates the widgets of the application main window
     */
    private void createShell() {
        Display display = Display.getDefault();
        shell = new Shell(display);
        shell.setText("Trivia");

        // window style
        Rectangle monitor_bounds = shell.getMonitor().getBounds();
        shell.setSize(new Point(monitor_bounds.width / 3,
                monitor_bounds.height / 4));
        shell.setLayout(new GridLayout());

        FontData fontData = new FontData();
        fontData.setStyle(SWT.BOLD);
        boldFont = new Font(shell.getDisplay(), fontData);

        // create window panels
        createFileLoadingPanel();
        createScorePanel();
        createQuestionPanel();
    }

    /**
     * Creates the widgets of the form for trivia file selection
     */
    private void createFileLoadingPanel() {
        final Composite fileSelection = new Composite(shell, SWT.NULL);
        fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
        fileSelection.setLayout(new GridLayout(4, false));

        final Label label = new Label(fileSelection, SWT.NONE);
        label.setText("Enter trivia file path: ");

        // text field to enter the file path
        final Text filePathField = new Text(fileSelection, SWT.SINGLE | SWT.BORDER);
        filePathField.setLayoutData(GUIUtils.createFillGridData(1));

        // "Browse" button
        final Button browseButton = new Button(fileSelection, SWT.PUSH);
        browseButton.setText("Browse");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                String path = GUIUtils.getFilePathFromFileDialog(shell);
                filePathField.setText(path);
            }
        });

        // "Play!" button
        final Button playButton = new Button(fileSelection, SWT.PUSH);
        playButton.setText("Play!");
        playButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    initGame(filePathField.getText());
                    newQuestionDisplay();
                } catch (IOException e) {
                    GUIUtils.showErrorDialog(shell, "File not exist or not in the correct format. Please try another!");
                }
            }
        });
    }

    /**
     * Creates the panel that displays the current score
     */
    private void createScorePanel() {
        Composite scorePanel = new Composite(shell, SWT.BORDER);
        scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
        scorePanel.setLayout(new GridLayout(2, false));

        final Label label = new Label(scorePanel, SWT.NONE);
        label.setText("Total score: ");

        // The label which displays the score; initially empty
        scoreLabel = new Label(scorePanel, SWT.NONE);
        scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
    }

    /**
     * Creates the panel that displays the questions, as soon as the game
     * starts. See the updateQuestionPanel for creating the question and answer
     * buttons
     */
    private void createQuestionPanel() {
        questionPanel = new Composite(shell, SWT.BORDER);
        questionPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        questionPanel.setLayout(new GridLayout(2, true));

        // Initially, only displays a message
        startupMessageLabel = new Label(questionPanel, SWT.NONE);
        startupMessageLabel.setText("No question to display, yet.");
        startupMessageLabel.setLayoutData(GUIUtils.createFillGridData(2));
    }

    /**
     * Serves to display the question and answer buttons
     */
    private void updateQuestionPanel(String question, List<String> answers) {
        // Save current list of answers.
        List<String> currentAnswers = answers;

        // clear the question panel
        Control[] children = questionPanel.getChildren();
        for (Control control : children) {
            control.dispose();
        }

        // create the instruction label
        instructionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
        instructionLabel.setText(lastAnswer + " Answer the following question:");
        instructionLabel.setLayoutData(GUIUtils.createFillGridData(2));

        // create the question label
        questionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
        questionLabel.setText(question);
        questionLabel.setFont(boldFont);
        questionLabel.setLayoutData(GUIUtils.createFillGridData(2));

        // create the answer buttons
        answerButtons.clear();
        for (int i = 0; i < 4; i++) {
            Button answerButton = new Button(questionPanel, SWT.PUSH | SWT.WRAP);
            answerButton.setText(answers.get(i));
            GridData answerLayoutData = GUIUtils.createFillGridData(1);
            answerLayoutData.verticalAlignment = SWT.FILL;
            answerButton.setLayoutData(answerLayoutData);
            answerButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent selectionEvent) {
                    if (isGameEnded)
                        return;
                    Button btnSource = (Button) selectionEvent.getSource(); // Button pressed
                    if (btnSource.getText().equals(correctAnswer)) {
                        lastAnswer = LastAnswer.CORRECT;
                        numberOfMistakes = 0; // Start from 0 again
                    } else {
                        lastAnswer = LastAnswer.WRONG;
                        numberOfMistakes++;
                    }
                    score += lastAnswer.getScore(); // Update score according to the answer
                    newQuestionDisplay();
                }
            });
            answerButtons.add(answerButton);
        }

        // create the "Pass" button to skip a question
        passButton = new Button(questionPanel, SWT.PUSH);
        passButton.setText("Pass");
        GridData data = new GridData(GridData.END, GridData.CENTER, true, false);
        data.horizontalSpan = 1;
        passButton.setLayoutData(data);
        passButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                if (isGameEnded)
                    return;
                if (helpers(Helper.PASS)) {
                    numOfPassedQuestions++;
                    newQuestionDisplay();
                }
            }
        });

        // create the "50-50" button to show fewer answer options
        fiftyFiftyButton = new Button(questionPanel, SWT.PUSH);
        fiftyFiftyButton.setText("50-50");
        data = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        data.horizontalSpan = 1;
        fiftyFiftyButton.setLayoutData(data);
        fiftyFiftyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                if (isGameEnded)
                    return;
                if (!fiftyInAction && helpers(Helper.FIFTY_FIFTY)) {
                    int counter = 0, randIndex;
                    while (counter < 2) {
                        randIndex = rand.nextInt(answers.size());
                        if (!answers.get(randIndex).equals(correctAnswer) && answerButtons.get(randIndex).getEnabled()) {
                            counter++;
                            answerButtons.get(randIndex).setEnabled(false);
                        }
                    }
                    fiftyInAction = true;
                    scoreLabel.setText(String.valueOf(score));
                }
            }
        });

        // two operations to make the new widgets display properly
        questionPanel.pack();
        questionPanel.getParent().layout();
    }

    /**
     * Opens the main window and executes the event loop of the application
     */
    private void runApplication() {
        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
        boldFont.dispose();
    }

    private void initGame(String path) throws IOException {
        // Init game attributes
        score = 0;
        numberOfMistakes = 0;
        numOfPassedQuestions = 0;
        isFirstHelper = EnumSet.allOf(Helper.class);
        lastAnswer = LastAnswer.EMPTY;
        isGameEnded = false;
        //rand.setSeed(0);

        // Read questions and answers
        BufferedReader bfRead = new BufferedReader(new FileReader(path));
        questionsAndAnswers = bfRead.lines().map(line -> line.split(SEP)).collect(Collectors.toList());
        bfRead.close();
        if (questionsAndAnswers.stream().anyMatch(x -> x.length != TOT_QUESTION_ANSWERS_LENGTH)) {
            throw new IOException();
        }
        Collections.shuffle(questionsAndAnswers, rand);
        currQuestionIndex = -1;
    }

    private void newQuestionDisplay() {
        scoreLabel.setText(String.valueOf(score));
        if (endGame()) {
            return;
        }
        // New question display
        currQuestionIndex++;
        String[] questionAnswersArray = questionsAndAnswers.get(currQuestionIndex);
        correctAnswer = questionAnswersArray[CORRECT_ANSWER_INDEX];
        String question = questionAnswersArray[QUESTION_INDEX];
        List<String> answers = Arrays.stream(questionAnswersArray).skip(QUESTION_INDEX + 1).collect(Collectors.toList());
        Collections.shuffle(answers);
        fiftyInAction = false;
        updateQuestionPanel(question, answers);
    }

    private boolean endGame() {
        if (currQuestionIndex >= questionsAndAnswers.size() || numberOfMistakes >= MAX_ERRORS) {
            String text = String.format("Your final score is %d after %d questions.", score, currQuestionIndex - numOfPassedQuestions);
            GUIUtils.showInfoDialog(shell, "GAME OVER", text);
            isGameEnded = true;
        }
        return isGameEnded;
    }

    private boolean helpers(Helper helper) {
        boolean isUseHelper = true;
        if (isFirstHelper.contains(helper)) {
            isFirstHelper.remove(helper);
        } else {
            if (score > 0)
                score += HELPER_SCORE_COST;
            else
                isUseHelper = false;
        }
        return isUseHelper;
    }
}

enum LastAnswer {
    EMPTY("", 0),
    CORRECT("Correct!", 3),
    WRONG("Wrong...", -2);

    private final String str;
    private final int score;

    LastAnswer(String str, int score) {
        this.str = str;
        this.score = score;
    }

    @Override
    public String toString() {
        return str;
    }

    public int getScore() {
        return score;
    }
}

enum Helper {
    PASS,
    FIFTY_FIFTY
}
