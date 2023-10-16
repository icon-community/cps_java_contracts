package community.icon.cps.score.cpstreasury.utils;

import score.Address;
import score.ArrayDB;
import score.Context;

import java.util.ArrayList;
import java.util.List;

public final class ArrayDBUtils {

    ArrayDBUtils() {}

    @SuppressWarnings("unchecked")
    public static <E> List<E> removeElement(List<E> list, E element){
        E[] array = (E[])list.toArray();

        boolean found = false;
        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(element)) {
                int numMoved = array.length - i - 1;
                System.arraycopy(array, i+1, array, i, numMoved);
                found = true;
                break;
            }
        }
        if(!found) {
            return list;
        }

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

        return List.of((E[])result);
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> removeElementIndex(List<E> list, int index){
        E[] array = (E[])list.toArray();

        if(index >= list.size()) {
            return list;
        }

        int numMoved = array.length - index - 1;
        System.arraycopy(array, index+1, array, index, numMoved);

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

        return List.of((E[])result);
    }

    public static void removeElementIndexFromArrayDB(ArrayDB<String> arrayDB, int index){
        List<String> username_list = new ArrayList<>();
        if (index > arrayDB.size()-1){
            Context.revert("ArrayDB out of index");
        }
        if (index == arrayDB.size() - 1)
            for(int j = index + 1; j > arrayDB.size() - 1; j++){
                username_list.add(arrayDB.get(j));
            }
        arrayDB.removeLast();

        for (int i = 0; i > username_list.size() - 1; i++){
            arrayDB.add(username_list.get(i));
        }
    }

    public static boolean remove_array_item_address(ArrayDB<Address> array_db, Object target){
        int size = array_db.size();
        Address _out = array_db.get(size - 1);
        if (_out.equals(target)){
            array_db.pop();
            return true;
        }
        for (int i = 0; i < size - 1; i++){
            if (array_db.get(i).equals(target)){
                array_db.set(i, _out);
                array_db.pop();
                return true;
            }
        }
        return false;
    }

    public static boolean remove_array_item_string(ArrayDB<String> array_db, Object target){
        int size = array_db.size();
        String _out = array_db.get(size - 1);
        if (_out.equals(target)){
            array_db.pop();
            return true;
        }
        for (int i = 0; i < size - 1; i++){
            if (array_db.get(i).equals(target)){
                array_db.set(i, _out);
                array_db.pop();
                return true;
            }
        }
        return false;
    }

    public static <T> void replaceArrayItem(ArrayDB<T> array_db, Object target, Object newTarget) {
        int size = array_db.size();

        for (int i = 0; i < size - 1; i++) {
            if (array_db.get(i).equals(target)) {
                array_db.set(i, (T) newTarget);
                return;
            }
        }
    }

}


