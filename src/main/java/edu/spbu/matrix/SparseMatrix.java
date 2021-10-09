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
  //в виде compressed sparse row
  private int height, width;
  private ArrayList<Double> value;
  private ArrayList<Integer> index_column; //массив индексов столбцов
  private ArrayList<Integer> ptr_row; //массив индексации строк, для индекса i хранит количество ненулевых элементов в строках до i-1 включительно


  public SparseMatrix(ArrayList<Double> value, ArrayList<Integer> ptr_row, ArrayList<Integer> index_column, int width, int height) {
    this.value = value;
    this.ptr_row = ptr_row;
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
      ptr_row.add(0);
      for (int i = 0 ; i < this.height ; i++){
        for (int j = 0; j < this.width; j++){
          if( (cur_val = sc.nextDouble()) != 0 ){
            value.add(cur_val);
            this.index_column.add(j); ///?????
          }
        }
        ptr_row.add(value.size());
      }


    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  public SparseMatrix transposeCSR() {
    ArrayList<Double> value_t = new ArrayList<>(this.value.size());
    ArrayList<Integer> index_column_t = new ArrayList<>(this.value.size());
    ArrayList<Integer> ptr_row_t = new ArrayList<>(this.width+1);
    ptr_row_t.add(0); //запирающий элемент


    ArrayList<ArrayList<Integer>> newcols = new ArrayList<>(width);
    ArrayList<ArrayList<Double>> newvals = new ArrayList<>(width);

    int k = 0;
    for (int i = 0 ; i < this.height ; i++){ //для каждой строки
      for (int j = 0 ; j < (this.ptr_row.get(i+1) - this.ptr_row.get(i)) ; j++){ //для каждого элемента в строке
        //this.value.get(k); // -- значение
        //this.index_column.get(k); // -- столбец в исходной
        //i // -- строка в исходной
        newvals.get(index_column.get(k)).add(value.get(k));
        newcols.get(index_column.get(k)).add(i);

        k++;
      }
    }
    for(int i = 0 ; i < width; i++ ){
      for (int j = 0 ; j < newvals.get(i).size(); j++){
        value_t.add( newvals.get(i).get(j) );
        index_column_t.add( newcols.get(i).get(j) );
      }
      ptr_row_t.add( ptr_row_t.get(i) + newvals.get(i).size() );
    }

    System.out.println(value_t);
    System.out.println(index_column_t);
    System.out.println(ptr_row_t);

    SparseMatrix t = new SparseMatrix(value_t,ptr_row_t,index_column_t,this.height,this.width);
    return t;
  }

  public SparseMatrix mul(SparseMatrix o) throws Exception {
    if (this.width != o.height) {throw new Exception("Не совпадают размеры матриц");}


    return null;
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

  public static void main(String[] args) {
    SparseMatrix m1 = new SparseMatrix("./SparceMatrix.txt");
    m1.transposeCSR();
  }



}


