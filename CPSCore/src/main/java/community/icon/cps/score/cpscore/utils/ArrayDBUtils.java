package community.icon.cps.score.cpscore.utils;

import score.ArrayDB;
import scorex.util.ArrayList;

import java.util.List;
import java.util.Map;

public final class ArrayDBUtils {
    public static void clearArrayDb(ArrayDB<?> array_db) {
        int size = array_db.size();
        for (int i = 0; i < size; i++) {
            array_db.pop();
        }

    }

    public static <T> void removeArrayItem(ArrayDB<T> array_db, Object target) {
        int size = array_db.size();
        T _out = array_db.get(size - 1);
        if (_out.equals(target)) {
            array_db.pop();
            return;
        }
        for (int i = 0; i < size - 1; i++) {
            if (array_db.get(i).equals(target)) {
                array_db.set(i, _out);
                array_db.pop();
                return;
            }
        }
    }


    public static <T> boolean containsInArrayDb(T value, ArrayDB<T> array) {
        boolean contains = false;
        if (array == null || value == null) {
            return contains;
        }

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) != null && array.get(i).equals(value)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public static <T> boolean containsInList(T value, List<T> array){
        boolean contains = false;
        if (array == null || value == null) {
            return contains;
        }

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) != null && array.get(i).equals(value)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public static String recordTxHash(byte[] tx_hash) {
        String tx_hash_string = encodeHexString(tx_hash);
        return "0x" + tx_hash_string;
    }

    public static String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public static <T> List<T> arrayDBtoList(ArrayDB<T> arraydb) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < arraydb.size(); i++) {
            list.add(arraydb.get(i));
        }
        return list;
    }

    public static <T> List<T> arrayToList(T[] array){
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++){
            list.add(array[i]);
        }
        return list;
    }

    public static void mergeSort(String[] array, int left, int right, Map<String, Integer> priorityVoteResult) {
        if (left < right) {

            int mid = (left + right) / 2;

            mergeSort(array, left, mid, priorityVoteResult);
            mergeSort(array, mid + 1, right, priorityVoteResult);

            merge(array, left, mid, right, priorityVoteResult);
        }
    }

    private static void merge(String[] array, int p, int q, int r, Map<String, Integer> priorityVoteResult) {

        int n1 = q - p + 1;
        int n2 = r - q;

        String[] L = new String[n1];
        String[] M = new String[n2];

        for (int i = 0; i < n1; i++)
            L[i] = array[p + i];
        for (int j = 0; j < n2; j++)
            M[j] = array[q + 1 + j];

        int i, j, k;
        i = 0;
        j = 0;
        k = p;

        while (i < n1 && j < n2) {
            if ((priorityVoteResult.get(L[i])) > (priorityVoteResult.get(M[j]))) {
                array[k] = L[i];
                i++;
            } else {
                array[k] = M[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            array[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            array[k] = M[j];
            j++;
            k++;
        }
    }
}


