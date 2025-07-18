package com.commerce.orders.utils;

public class CommonFunctions {
    public class LongToStringConverter {
        public static String convertLongToString(Long number) {
            if (number == null) {
                return null; // or return "null" as string if preferred
            }
            return number.toString();
        }

        public static String longToString(long number) {
            return Long.toString(number);
        }
    }
}
