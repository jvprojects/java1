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

class GraphicCalculator extends JFrame implements KeyListener {
    // Some calculator constants
    private final byte MAIN_SCREEN = 0;
    private final byte EXTRA_SCREEN = 1;
    private final byte MAIN_SCREEN_MAX_SYMBOLS = 15; // max chars on main (big) screen.
    private final byte MAX_OPERATIONS = 64; // max operations count (64 by default but might be more).

    // primitives
    private byte storedCount = 0; // stored number of arithmetic operations
    private char lastOperation = 0; // last arithmetic operation
    private boolean dotUsed = false; // is dot was type on the big screen
    private boolean isDecimal = false; // is digit was last typed symbol
    private char[] storedOperation = new char[MAX_OPERATIONS]; // stored operators like divide, multiplication, substraction etc.
    private double[] storedNumber = new double[MAX_OPERATIONS]; // stored operands. every operand has own operator (same indexes).
    private boolean[] hasItem = new boolean[MAX_OPERATIONS]; // Is the current index and has an item (operator+operand)

    // objects
    private StringBuilder[] screenBuffer = new StringBuilder[]{ new StringBuilder(), new StringBuilder() }; // buffers of 2 both displays

    /* displays must be exactly JTextField because of text copy and extension out of calculator bounds with possible to scroll it with mouse (by using selection) */
    private JTextField[] outputArea = { new JTextField("0", SwingConstants.RIGHT), new JTextField("", SwingConstants.RIGHT) };

    private JPanel buttonPanel = new JPanel(new GridLayout(5,4)); // panel with buttons with grid layout
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
        /* must be set before setting buttons */
        setupButtonsPanel();

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

        isDecimal = true;
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
        case '\u00d7': // multiplication
            multiplicationHandler();
            break;
        case '+':
            additionHandler();
            break;
        case '-':
            substractionHandler();
            break;
        case '=':
            resultHandler();
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
        if(storedCount >= MAX_OPERATIONS || (lastOperation == '\u00f7' && !isDecimal)) return;

        double currentValue = getScreenSymbolsNum(MAIN_SCREEN) != 0 ? Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()) : 0.0d;

        if(currentValue == 0.0d && isDecimal) {
            showResult("Cannot divide by zero");
            return;
        }

        if(!pushValueToBuffer(currentValue, '\u00f7')) return;

        clearInfoOnMainScreen();
        lastOperation = '\u00f7';
        pushValueToStack(currentValue, lastOperation);
    }


    void multiplicationHandler() {
        if(storedCount >= MAX_OPERATIONS || (lastOperation == '\u00d7' && !isDecimal)) return;
        
        double currentValue = getScreenSymbolsNum(MAIN_SCREEN) != 0 ? Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()) : 0.0d;
        
        if(!pushValueToBuffer(currentValue, '\u00d7')) return;
        
        clearInfoOnMainScreen();
        lastOperation = '\u00d7';
        pushValueToStack(currentValue, lastOperation);
    }


    void substractionHandler() {
        if(storedCount >= MAX_OPERATIONS || (lastOperation == '-' && !isDecimal)) return;
        
        double currentValue = getScreenSymbolsNum(MAIN_SCREEN) != 0 ? Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()) : 0.0d;
        
        if(!pushValueToBuffer(currentValue, '-')) return;
        
        clearInfoOnMainScreen();
        lastOperation = '-';
        pushValueToStack(currentValue, lastOperation);
    }


    void additionHandler() {
        if(storedCount >= MAX_OPERATIONS || (lastOperation == '+' && !isDecimal)) return;
        
        double currentValue = getScreenSymbolsNum(MAIN_SCREEN) != 0 ? Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()) : 0.0d;
        
        if(!pushValueToBuffer(currentValue, '+')) return;
        
        clearInfoOnMainScreen();
        lastOperation = '+';
        pushValueToStack(currentValue, lastOperation);
    }


    void plusMinusHandler() {
        if(getScreenSymbolsNum(MAIN_SCREEN) <= 0) return;

        double currentValue = Double.parseDouble(screenBuffer[MAIN_SCREEN].toString());
        currentValue *= -1.0d;

        if(currentValue < 0.0d) {
            screenBuffer[MAIN_SCREEN].insert(0, '-');
        } else {
            screenBuffer[MAIN_SCREEN].replace(0, 1, "");
        }

        updateScreen(MAIN_SCREEN);
    }


    void squareRootHandler() {
        if(lastOperation > 0 || getScreenSymbolsNum(MAIN_SCREEN) <= 0) return;

        double currentValue = Math.sqrt(Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()));
        showResult(currentValue);
    }


    void resultHandler() {
        if(getScreenSymbolsNum(MAIN_SCREEN) == 0 && getScreenSymbolsNum(EXTRA_SCREEN) == 0) return;
        if(getScreenSymbolsNum(MAIN_SCREEN) == 0 && lastOperation == '\u00f7') {
            showResult("Cannot divide by zero");
            return;
        }

        double lastValue = getScreenSymbolsNum(MAIN_SCREEN) != 0 ? Double.parseDouble(screenBuffer[MAIN_SCREEN].toString()) : 0.0d;
        
        if(lastValue == 0.0d && lastOperation == '\u00f7') {
            showResult("Cannot divide by zero");
            return;
        }

        pushValueToStack(lastValue);

        byte i;

        // check for special operations which should happen first, like multiplication or division
        for(i = 0; i < storedCount; ++i) {
            if(!hasItem[i])
                continue;

            switch(storedOperation[i]) {
            case '\u00d7': // multiplication
                storedNumber[i] *= storedNumber[i+1];
                lastValue = storedNumber[i+1] = storedNumber[i];
                hasItem[i] = false; // Set "invisible" the item at the given position in the list
                break;
            case '\u00f7': // division
                storedNumber[i] /= storedNumber[i+1];
                lastValue = storedNumber[i+1] = storedNumber[i];
                hasItem[i] = false; // Set "invisible" the item at the given position in the list
                break;
            }
        }
        // check for lasts operations, like addition or substraction
        for(i = 0; i < storedCount; ++i) {
            if(!hasItem[i])
                continue;

            switch(storedOperation[i]) {
            case '+':
                storedNumber[i] += storedNumber[i+1];
                lastValue = storedNumber[i+1] = storedNumber[i];
                hasItem[i] = false; // Set "invisible" the item at the given position in the list
                break;
            case '-':
                storedNumber[i] -= storedNumber[i+1];
                lastValue = storedNumber[i+1] = storedNumber[i];
                hasItem[i] = false; // Set "invisible" the item at the given position in the list
                break;
            }
        }
        showResult(lastValue);
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
            isDecimal = false;
        }
        updateScreen(MAIN_SCREEN, result);
    }


    void setDefaults() {
        showResult();
    }


    void clearInfoOnMainScreen() {
        clearScreen(MAIN_SCREEN);
        updateScreen(EXTRA_SCREEN);

        dotUsed = false;
        isDecimal = false;
    }


    void showResult() {
        dotUsed = false;
        storedCount = 0;
        isDecimal = false;
        lastOperation = 0;
        clearScreen(MAIN_SCREEN);
        clearScreen(EXTRA_SCREEN);
        Arrays.fill(hasItem, false);
    }


    void showResult(double valueToDisplay) {
        dotUsed = false;
        storedCount = 0;
        isDecimal = false;
        lastOperation = 0;
        showOn(valueToDisplay);
        Arrays.fill(hasItem, false);
        clearScreen(EXTRA_SCREEN);
        screenBuffer[MAIN_SCREEN].setLength(0);
    }


    void showResult(String valueToDisplay) {
        dotUsed = false;
        storedCount = 0;
        isDecimal = false;
        lastOperation = 0;
        showOn(valueToDisplay);
        Arrays.fill(hasItem, false);
        clearScreen(EXTRA_SCREEN);
        screenBuffer[MAIN_SCREEN].setLength(0);
    }


    void clearScreen(byte screen) {
        if(screen == MAIN_SCREEN) {
            updateScreen(MAIN_SCREEN, "0");
            screenBuffer[MAIN_SCREEN].setLength(0);
        } else {
            screenBuffer[EXTRA_SCREEN].setLength(0);
            updateScreen(EXTRA_SCREEN);
        }
    }


    void updateScreen(byte screen) {
        outputArea[screen].setText(screenBuffer[screen].toString());
    }


    void updateScreen(byte screen, String text) {
        outputArea[screen].setText(text);
    }


    void showOn(double result) {
        if(!isInteger(result)) {
            outputArea[MAIN_SCREEN].setText(String.valueOf(result));
        } else {
            outputArea[MAIN_SCREEN].setText(String.valueOf(Math.round(result)));
        }
    }


    void showOn(String result) {
        outputArea[MAIN_SCREEN].setText(result);
    }


    boolean pushValueToBuffer(double value, char operation) {
        if(isDecimal) {
            appendToExtraScreen(value, operation);
        } else {
            if(getScreenSymbolsNum(EXTRA_SCREEN) == 0) {
                appendToExtraScreen(value, operation);
            } else {
                replaceLastOperation(operation);
                updateScreen(EXTRA_SCREEN);
                return false;
            }
        }
        return true;
    }


    void appendToExtraScreen(double value, char operation) {
        if(isInteger(value)) {
            screenBuffer[EXTRA_SCREEN].append(Double.valueOf(value).longValue());
        } else {
            screenBuffer[EXTRA_SCREEN].append(value);
        }

        screenBuffer[EXTRA_SCREEN].append(' ');
        screenBuffer[EXTRA_SCREEN].append(operation);
        screenBuffer[EXTRA_SCREEN].append(' ');
    }


    void pushValueToStack(double value, char operation) {
        hasItem[storedCount] = true;
        storedNumber[storedCount] = value;
        storedOperation[storedCount] = operation;
        storedCount++;
    }


    void pushValueToStack(double value) {
        hasItem[storedCount] = true;
        storedNumber[storedCount] = value;
    }


    void replaceLastOperation(char operation) {
        screenBuffer[EXTRA_SCREEN].setLength(getScreenSymbolsNum(EXTRA_SCREEN) - 2);
        screenBuffer[EXTRA_SCREEN].append(operation);
        screenBuffer[EXTRA_SCREEN].append(' ');
    }


    int getScreenSymbolsNum(byte screen) {
        return screenBuffer[screen].length();
    }


    boolean isInteger(double num) {
        return num % 1 == 0;
    }
    
    
    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped");
    }
    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed");
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
    }
}