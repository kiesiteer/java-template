package edu.spbu.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by artemaliev on 07/09/15.
 */
public class IntSort {
  public static void qsort (int array[], int left, int right){
    int pivot = array[(left + right) / 2];
    int i = left;
    int j = right;
    while (i <= j){

      while (array[i] < pivot) i++;
      while (pivot < array[j]) j--;

      if (i <= j){
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        j--;
        i++;
      }
    }
    if (left < j) qsort(array ,left, j);
    if (right > i) qsort(array, i, right);
  }

  public static void sort (int array[]) {
    qsort(array, 0, array.length-1);
    //Arrays.sort(array);
  }

  public static void sort (List<Integer> list) {
    Collections.sort(list);
  }
}
