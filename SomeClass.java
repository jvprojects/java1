/**
 * @author Kitaev Boris
 * @version 0.0.1 dated 25 Aug 2016
 */

import java.text.SimpleDateFormat

class SomeClass
{
    public static void main(String[] args) {
        byte a = 8;
        short b = 4;
        int c = 2;
        long d = 1;
        float e = 16.0f;
        double f = 32.0d;
        char g = 'j';

        SomeClass obj = new SomeClass();
        boolean IsInRange = obj.InRange(a, b);
        String Buffer = IsInRange ? "true" : "false";
        int RetVal = obj.CalculateExpression(a, b, c, d);
        System.out.println("CalculateExpression Method return value: " + RetVal);
        System.out.println("a = " + a + " b = " + b);
        System.out.println("10 <= a + b <= 20 ? " + Buffer);
    }

    public int CalculateExpression(byte a, short b, int c, long d) {
        return (int)(a * (b + (c / d)));
    }

    public boolean InRange(byte a, short b) {
        return (short)(a + b) > 10 ? (short)(a + b) > 20 ? false : true : false;
    }

    public boolean IsLeapYear() {
        SimpleDateFormat DateObj = new SimpleDateFormat("HH:mm:ss");
    }
}