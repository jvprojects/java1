/**
 * @author Kitaev Boris
 * @version 0.5 dated 17 Sep 2016
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

class GraphicCalculator extends JFrame {
    // Some calculator constants
    private final byte MAIN_SCREEN = 0;
    private final byte EXTRA_SCREEN = 1;
    private final byte MAIN_SCREEN_MAX_SYMBOLS = 15;
    private final byte EXTRA_SCREEN_MAX_SYMBOLS = 32;
    private final byte MAX_OPERATIONS = 24;

    // primitives
    private byte storedStage = 0;
    private char lastOperation = 0;
    private boolean dotUsed = false;
    private char[] storedOperations = new char[MAX_OPERATIONS];
    private double[] storedNumbers = new double[MAX_OPERATIONS];

    // objects
    private StringBuilder[] screenBuffer = new StringBuilder[]{ new StringBuilder(), new StringBuilder() }; // current number of digits on the display
    private JTextField[] outputArea = { new JTextField("0", SwingConstants.RIGHT), new JTextField("", SwingConstants.RIGHT) };
    private JPanel buttonPanel = new JPanel(new GridLayout(5,4));
    private JButton[] cDigits = new JButton[20];


    public static void main(String[] args) {
        new GraphicCalculator();
    }


    GraphicCalculator() {
        // setup calculator components
        setupWindow();
        setupButtons();
        setupMainDisplay();
        setupExtraDisplay();

        // show the calculator
        setVisible(true);
    }


    void setupWindow() {
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200, 200, 349, 558); // start x, start y, width, height
        setBackground(new Color(255, 255, 255));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setLayout(null);
        setResizable(false);
    }


    void setupMainDisplay() {
        outputArea[MAIN_SCREEN].setSize(343, 38);
        outputArea[MAIN_SCREEN].setLocation(0, 26);
        outputArea[MAIN_SCREEN].setEditable(false);
        outputArea[MAIN_SCREEN].setBackground(new Color(235, 235, 235));
        outputArea[MAIN_SCREEN].setHorizontalAlignment(JTextField.RIGHT);
        outputArea[MAIN_SCREEN].setFont(new Font("Arial", Font.LAYOUT_RIGHT_TO_LEFT, 32));
        add(outputArea[MAIN_SCREEN]);
    }


    void setupExtraDisplay() {
        outputArea[EXTRA_SCREEN].setSize(343, 27);
        outputArea[EXTRA_SCREEN].setEditable(false);
        outputArea[EXTRA_SCREEN].setBackground(new Color(245, 245, 245));
        outputArea[EXTRA_SCREEN].setHorizontalAlignment(JTextField.RIGHT);
        outputArea[EXTRA_SCREEN].setFont(new Font("Arial", Font.LAYOUT_RIGHT_TO_LEFT, 18));
        add(outputArea[EXTRA_SCREEN]);
    }


    void setupButtons() {
        setupButtonsPanel(); // must be set before setting buttons

        final char digitalGrid[] = { 'C', '\u221a', '\u00ab', '\u00f7',
                                     '7',    '8',      '9',   '\u00d7', 
                                     '4',    '5',      '6',      '-', 
                                     '1',    '2',      '3',      '+', 
                                     '±',    '0',      '.',      '=' };

        for(byte i = 0; i < digitalGrid.length; i++) {
            cDigits[i] = new JButton(Character.toString(digitalGrid[i]));
            cDigits[i].setBorder(new EtchedBorder());
            cDigits[i].setBackground(new Color(225, 225, 225));
            cDigits[i].setForeground(new Color(64, 64, 64));
            cDigits[i].setFocusPainted(false);
            cDigits[i].setFont(new Font("Serif", Font.BOLD, i == 1 ? 33 : 40));
            setupListener(cDigits[i]);
            buttonPanel.add(cDigits[i]);

            add(buttonPanel);
        }
    }


    void setupButtonsPanel() {
        buttonPanel.setSize(345, 460);
        buttonPanel.setLocation(0, 64);
    }


    void setupListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                char pressedButton = event.getActionCommand().charAt(0);

                if(pressedButton >= '0' && pressedButton <= '9') {
                    digitsHandler(pressedButton);
                } else {
                    operationsHandler(pressedButton);
                }
            }
        });
    }


    void digitsHandler(char digit) {
        if(getScreenSymbolsNum(MAIN_SCREEN) >= MAIN_SCREEN_MAX_SYMBOLS) return;

        lastOperation = 0;
        screenBuffer[MAIN_SCREEN].append(digit);
        updateScreen(MAIN_SCREEN, screenBuffer[MAIN_SCREEN].toString());
    }


    void operationsHandler(char operation) {
        switch(operation) {
        case '\u00ab': // backspace
            backspaceHandler();
            break;
        case 'C':
            setDefaults();
            break;
        case '±':
            plusMinusHandler();
            break;
        case '\u221a': // square root
            squareRootHandler();
            break;
        case '.':
            dotHandler();
            break;
        case '\u00f7': // division
            divisionHandler();
            break;
        }
    }


    void dotHandler() {
        if(dotUsed) return;
        if(getScreenSymbolsNum(MAIN_SCREEN) == 0) digitsHandler('0');

        dotUsed = true;
        digitsHandler('.');
    }


    void divisionHandler() {
        if(storedStage >= MAX_OPERATIONS || lastOperation == '\u00f7') return;

        storedNumbers[storedStage] = Double.parseDouble(screenBuffer[MAIN_SCREEN].toString());
        lastOperation = storedOperations[storedStage] = '\u00f7';

        pushToExtraScreen(storedNumbers[storedStage], storedOperations[storedStage]);
        updateScreen(EXTRA_SCREEN);
        
        storedStage++;
    }


    void plusMinusHandler() {
        if(getScreenSymbolsNum(MAIN_SCREEN) <= 0) return;

        double temp = Double.parseDouble(screenBuffer[MAIN_SCREEN].toString());
        temp *= -1.0d;

        if(temp < 0.0d) {
            screenBuffer[MAIN_SCREEN].insert(0, '-');
        } else {
            screenBuffer[MAIN_SCREEN].replace(0, 1, "");
        }

        updateScreen(MAIN_SCREEN);
    }


    void squareRootHandler() {
        if(lastOperation > 0 || getScreenSymbolsNum(MAIN_SCREEN) <= 0) return;

        double temp = Math.sqrt(Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()));
        showResult(temp);
        setDefaults(temp);
    }


    void backspaceHandler() {
        if(getScreenSymbolsNum(MAIN_SCREEN) <= 0) return;

        char deletedChar = screenBuffer[MAIN_SCREEN].charAt(getScreenSymbolsNum(MAIN_SCREEN) - 1);
        screenBuffer[MAIN_SCREEN].setLength(getScreenSymbolsNum(MAIN_SCREEN) - 1); // уменьшаем длинну строки на 1 символ
            
        String result = null;
        if(getScreenSymbolsNum(MAIN_SCREEN) > 0) {
            result = screenBuffer[MAIN_SCREEN].toString();
                
            if(deletedChar == '.') dotUsed = false;
        } else {
            result = "0";
            dotUsed = false;
        }
        updateScreen(MAIN_SCREEN, result);
    }


    void setDefaults() {
        dotUsed = false;
        storedStage = 0;
        lastOperation = 0;
        updateScreen(MAIN_SCREEN, "0");
        Arrays.fill(storedNumbers, 0.0d);
        screenBuffer[MAIN_SCREEN].setLength(0);
        screenBuffer[EXTRA_SCREEN].setLength(0);
        updateScreen(EXTRA_SCREEN);
    }


    void setDefaults(double valueToDisplay) {
        dotUsed = false;
        storedStage = 0;
        lastOperation = 0;
        showResult(valueToDisplay);
        Arrays.fill(storedNumbers, 0.0d);
        screenBuffer[EXTRA_SCREEN].setLength(0);
        updateScreen(EXTRA_SCREEN);
    }
    
    
    void resetMainScreen() {
        screenBuffer[MAIN_SCREEN].setLength(0);
    }


    void updateScreen(byte screen) {
        outputArea[screen].setText(screenBuffer[screen].toString());
    }


    void updateScreen(byte screen, String text) {
        outputArea[screen].setText(text);
    }


    void showResult(double result) {
        if(!isInteger(result)) {
            outputArea[MAIN_SCREEN].setText(String.valueOf(result));
        } else {
            outputArea[MAIN_SCREEN].setText(String.valueOf(Math.round(result)));
        }
    }


    void pushToExtraScreen(double value, char operation) {
        screenBuffer[EXTRA_SCREEN].append(value);
        screenBuffer[EXTRA_SCREEN].append(' ');
        screenBuffer[EXTRA_SCREEN].append(operation);
        screenBuffer[EXTRA_SCREEN].append(' ');
    }


    int getScreenSymbolsNum(byte screen) {
        return screenBuffer[screen].length();
    }


    boolean isInteger(double num) {
        return num % 1 == 0;
    }
}