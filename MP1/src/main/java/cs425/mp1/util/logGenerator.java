package cs425.mp1.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class logGenerator implements Iterable<String> {

    private Random ran = new Random();

    /**
     * numOfRow of the random file
     */
    private int numOfRow;

    /**
     * length of the string with alphanum and symbols
     */
    private int stringLength;

    /**
     * Get string formatted random log
     *
     * @param rows      Number of rows
     * @param rowLength Length of the string with alphanum and symbols
     */
    public logGenerator(int rows, int rowLength) {
        this.numOfRow = rows;
        this.stringLength = rowLength;
    }

    public ArrayList<String> generateTexts() {
        ArrayList<String> res = new ArrayList<>(this.numOfRow);
        for (int i = 0; i < numOfRow; i++) {
            int r1 = ran.nextInt();
            String r2 = r2(stringLength, ran);
            double r3 = ran.nextDouble();
            double r4 = ran.nextDouble();
            double r5 = ran.nextDouble();
            res.add(r1 + r2 + r3 + r4 + r5);
        }
        return res;
    }

    public String generateAssembledText() {
        return String.join("\n", this.generateTexts());
    }

    private String r2(int length, Random ran) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "/-_[]()";

        String character = upper + lower + digits + symbols;

        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            randomString.append(character.charAt(ran.nextInt(character.length())));
        }

        return randomString.toString();
    }

    /**
     * Returns an iterator over elements of type {@code String}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                // Always have next random text
                return true;
            }

            @Override
            public String next() {
                return generateAssembledText();
            }
        };
    }
}
