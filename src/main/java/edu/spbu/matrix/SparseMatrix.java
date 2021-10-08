package edu.spbu.matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{
  private int height, width;
  private ArrayList<Double> value;
  private ArrayList<Integer> index_row;
  private ArrayList<Integer> index_column;


  public SparseMatrix(ArrayList<Double> value, ArrayList<Integer> index_row, ArrayList<Integer> index_column, int width, int height) {
    this.value = value;
    this.index_row = index_row;
    this.index_column = index_column;
    this.width = width;
    this.height = height;
  }

  /**
   * загружает матрицу из файла
   * @param fileName
   */
  public SparseMatrix(String fileName) {
    try {
      Scanner sc = new Scanner(new FileReader(fileName));
      if (!sc.hasNextLine()){
        throw new Exception("Пустой файл");
      }

      String line = sc.nextLine();
      this.width = 1;
      this.height = 1;
      for(int i = 0 ; i< line.length() ; i++){
        if (line.charAt(i) == ' '){
          this.width++;
        }
      }
      while( sc.hasNextLine() ){
        line = sc.nextLine();
        this.height++;
      }
      sc.reset();

      double cur_val = 0;
      for (int i = 0 ; i < this.height ; i++){
        for (int j = 0 ;j < this.width ; j++)
          if( (cur_val = sc.nextDouble()) != 0 ){
            this.value.add(cur_val);
            this.index_column.add(j);
            this.index_row.add(i);
          }
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }




  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   * @param o
   * @return
   */
  @Override public Matrix mul(Matrix o)
  {
    return null;
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o)
  {
    return null;
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    return false;
  }
}
