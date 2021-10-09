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
  public int height, width;
  public ArrayList<Double> value;
  public ArrayList<Integer> index_column; //массив индексов столбцов
  public ArrayList<Integer> ptr_row; //массив индексации строк, для индекса i хранит количество ненулевых элементов в строках до i-1 включительно


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
    ptr_row = new ArrayList<Integer>();
    value = new ArrayList<Double>();
    index_column = new ArrayList<Integer>();
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
      sc = new Scanner(new FileReader(fileName));

      double cur_val = 0;
      ptr_row.add(0);

      for (int i = 0 ; i < this.height ; i++){
        for (int j = 0; j < this.width; j++){
          cur_val = sc.nextDouble();
          if(cur_val != 0 ){
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
    for (int i = 0 ; i < width ; i++){
      newcols.add(new ArrayList<Integer>());
      newvals.add(new ArrayList<Double>());
    }

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



    SparseMatrix t = new SparseMatrix(value_t,ptr_row_t,index_column_t,this.height,this.width);
    return t;
  }

  private SparseMatrix mul(SparseMatrix o) throws Exception {
    if (this.width != o.height) {throw new Exception("Не совпадают размеры матриц");}
    ArrayList<Integer> res_ptr_row = new ArrayList<>();
    ArrayList<Integer> res_index_column = new ArrayList<>();
    ArrayList<Double> res_value = new ArrayList<>();
    res_ptr_row.add(0);//запирающий элемент
    SparseMatrix ot = o.transposeCSR();

    int k = 0;
    int k2 = 0;
    for (int i = 0 ; i < this.height ; i++){ //для каждой строки левой
      k2=0;
      for (int i2 = 0 ; i2 < ot.height ; i2++){ //для каждого столбца правой (строки правой транспонированной)
        double x = 0; // результат перемножения векторов
        for (int j = 0 ; j < (this.ptr_row.get(i+1) - this.ptr_row.get(i)) ; j++){ //для каждого элемента в строке левой
          for (int j2 = 0 ; j2 < (ot.ptr_row.get(i2+1) - ot.ptr_row.get(i2)) ; j2++){ //для каждого элемента в столбе правой
            if (this.index_column.get(k+j) == ot.index_column.get(k2+j2)){
              x += this.value.get(k+j)*ot.value.get(k2+j2);
              continue;
            }
          }
        }
        //добавить x в матрицу
        if (x!=0){
          res_value.add(x);
          res_index_column.add(i2);
        }
        k2 += (ot.ptr_row.get(i2+1) - ot.ptr_row.get(i2));
      }
      res_ptr_row.add(res_value.size());
      k += (this.ptr_row.get(i+1) - this.ptr_row.get(i));
    }

    SparseMatrix res = new SparseMatrix(res_value,res_ptr_row,res_index_column,o.width,this.height);
    return res;
  }

  private SparseMatrix mul(DenseMatrix o) throws Exception{
    if (this.width != o.height) {throw new Exception("Не совпадают размеры матриц");}
    ArrayList<Integer> res_ptr_row = new ArrayList<>();
    ArrayList<Integer> res_index_column = new ArrayList<>();
    ArrayList<Double> res_value = new ArrayList<>();
    res_ptr_row.add(0);//запирающий элемент
    int k = 0;
    for (int i = 0 ; i < this.height ; i++) { //для каждой строки левой
      for (int l = 0 ; l < o.width ; l++){ //для каждого столбца правой
        double x = 0;
        for (int j = 0 ; j < (this.ptr_row.get(i+1) - this.ptr_row.get(i)) ; j++){ //для каждого элемента в строке левой
          x+= value.get(k+j) * o.value[index_column.get(k+j)][l];
        }
        if (x != 0){
          res_value.add(x);
          res_index_column.add(l);
        }
      }
      res_ptr_row.add(res_value.size());
      k += (this.ptr_row.get(i+1) - this.ptr_row.get(i));
    }

    SparseMatrix res = new SparseMatrix(res_value,res_ptr_row,res_index_column,o.width,this.height);
    return res;
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


  private boolean equals(SparseMatrix o) {
    if (this == o) return true;
    if (this.height != o.height && this.width != o.width) return false;
    else {
      return (this.value.equals(o.value) && this.index_column.equals(o.index_column) && this.ptr_row.equals(o.ptr_row));
    }
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    return false;
  }

  public static void main(String[] args) throws Exception {
    SparseMatrix m1 = new SparseMatrix("./SparseMatrix.txt");
    DenseMatrix d1 = new DenseMatrix("./DenseMatrix3.txt");
    SparseMatrix m2 = new SparseMatrix("./sm1xdm3_res.txt");
    SparseMatrix m3 = m1.mul(d1);
    System.out.println(m3.equals(m2));
    //System.out.println(m1.equals(m2));
    //SparseMatrix m3 = m1.transposeCSR();
    //System.out.println(m1.equals(m3));
  }



}


