/**
 * @author Kitaev Boris
 * @version 0.5 dated 17 Sep 2016
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

class GraphicCalculator extends JFrame {
    int displayDigits = 0; // current number of digits on the display
    double displayNumber = 0.0d; // current number of digits on the display
    StringBuilder displayBuffer = new StringBuilder(12); // current number of digits on the display
    private JTextArea outputArea = null;
    JButton[] cDigits = new JButton[20];

    public static void main(String[] args) {
        new GraphicCalculator();
    }
    
    GraphicCalculator() {
        // set layout to manual
        setLayout(null);

        // setup calculator components
        setupWindow();
        setupButtons();
        setupOutputArea();

        // show the frame
        setVisible(true);
    }
    
    
    void setupWindow() {
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200, 200, 345, 522); // start x, start y, width, height
        setBackground(new Color(255, 255, 255));
        setResizable(false);
    }
    
    
    void setupButtons() { 
        byte x = 0, y = 0;
        final char digitalGrid[] = { 'C', 247, '\u00ab', '\u00f7', '7', '8', '9', '\u00d7', '4', '5', '6', '-', '1', '2', '3', '+', '±', '0', '.', '=' };
        for(byte i = 0; i < digitalGrid.length; i++) {
            cDigits[i] = new JButton((i == 1) ? ("x" + Character.toString('\u00b2')) : Character.toString(digitalGrid[i]));
            cDigits[i].setSize(85, 85);
            cDigits[i].setLocation(x*85, y*85+64);
            cDigits[i].setBorder(new EtchedBorder());
            cDigits[i].setBackground(new Color(225, 225, 225));
            cDigits[i].setForeground(new Color(64, 64, 64));
            cDigits[i].setFocusPainted(false);
            cDigits[i].setFont(new Font("Arial", Font.PLAIN, 40));
            setupListener(cDigits[i]);
            add(cDigits[i]);

            if((i + 1) % 4 == 0) {
                x = 0;
                y++;
            } else {
                x++;
            }
        }
    }
    
    
    void setupListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                char charbutton = event.getActionCommand().charAt(0);

                if(charbutton >= '0' && charbutton <= '9' && displayDigits < 10) {
                    handleDigit(charbutton);
                } else {
                    handleOperation(charbutton);
                }
            }
        });
    }
    
    
    void setupOutputArea() {
        outputArea = new JTextArea("0");
        outputArea.setSize(365, 64);
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(244, 244, 244));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Arial", Font.LAYOUT_RIGHT_TO_LEFT, 55));
        //outputArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        //outputArea.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(outputArea);
    }
    
    
    void handleDigit(char digit) {
        displayBuffer.append(digit);
        displayNumber = Double.parseDouble(displayBuffer.toString());
        if(displayDigits > 0) {
            outputArea.append(String.valueOf(digit));
        } else {
            updateDisplay(String.valueOf(digit));
        }
        displayDigits++;
    }
    
    
    void handleOperation(char operation) {
        switch(operation) {
        case '\u00ab':
            if(displayDigits <= 0) return;

            displayBuffer.setLength(--displayDigits);
            String result = (displayDigits > 0) ? String.valueOf(displayBuffer) : "0";
            displayNumber = (displayDigits > 0) ? Double.parseDouble(result) : 0;
            updateDisplay(result);
            break;
        case 'C':
            displayDigits = 0;
            displayNumber = 0;
            updateDisplay("0");
            displayBuffer.setLength(0);
            break;
        case '±':
            displayNumber *= -1;
            updateDisplay(String.valueOf(displayNumber));
            break;
        /* пока не работает
        case "x?":
            displayNumber *= displayNumber;
            String result2 = String.valueOf(displayNumber);
            displayBuffer.replace(0, displayDigits, result2);
            updateDisplay(result2);
            break;
        */
        }

    }
    
    
    void updateDisplay(String text) {
        if(!isInteger(displayNumber)) {
            outputArea.setText(text.valueOf(displayNumber));
        } else {
            outputArea.setText(text.valueOf(Math.round(displayNumber)));
        }
    }
    
    
    boolean isInteger(double num) {
        return num % 1 == 0;
    }
}