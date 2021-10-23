package edu.spbu.matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
  @Override public Matrix mul(Matrix o) throws Exception {
    if (o instanceof DenseMatrix){
      return this.mul((DenseMatrix) o);
    }
    else if (o instanceof SparseMatrix){
      return this.mul((SparseMatrix) o);
    }
    else return null;
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o) throws Exception {
    if (o instanceof SparseMatrix){
      return this.dmul((SparseMatrix) o);
    }
    return null;
  }
  private SparseMatrix dmul(SparseMatrix o) throws Exception {
    if (this.width != o.height) {throw new Exception("Не совпадают размеры матриц");}
    ArrayList<Integer> res_ptr_row = new ArrayList<>();
    ArrayList<Integer> res_index_column = new ArrayList<>();
    ArrayList<Double> res_value = new ArrayList<>();
    res_ptr_row.add(0);
    SparseMatrix ot = o.transposeCSR();

    int upperHeight = this.height / 2;
    int lowerHeight = this.height - upperHeight;
    ArrayList<Integer> upper_ptr_row = new ArrayList<Integer> (this.ptr_row.subList(0,upperHeight+1));
    ArrayList<Integer> upper_index_column = new ArrayList<Integer>(this.index_column.subList(0, upper_ptr_row.get(upper_ptr_row.size()-1)));
    ArrayList<Double> upper_value = new ArrayList<Double>(this.value.subList(0, upper_ptr_row.get(upper_ptr_row.size()-1)) );


    ArrayList<Integer> lower_ptr_row = new ArrayList<Integer> (this.ptr_row.subList(upperHeight+1,this.height+1));
    for (int k = 0 ; k <lower_ptr_row.size() ; k++){
      lower_ptr_row.add(k , lower_ptr_row.get(k) - upper_ptr_row.get( upper_ptr_row.size()-1  ) );
      lower_ptr_row.remove(k+1);
    }
    lower_ptr_row.add(0, 0);
    ArrayList<Integer> lower_index_column = new ArrayList<Integer>(this.index_column.subList(upper_ptr_row.get(upper_ptr_row.size()-1), this.index_column.size()) );
    ArrayList<Double> lower_value = new ArrayList<Double> (this.value.subList(upper_ptr_row.get(upper_ptr_row.size()-1), this.index_column.size()) );

    SparseMatrix upper = new SparseMatrix(upper_value,upper_ptr_row,upper_index_column,this.width,upperHeight);
    SparseMatrix lower = new SparseMatrix(lower_value,lower_ptr_row,lower_index_column,this.width,lowerHeight);
    SparseMatrix res [] = new SparseMatrix[2];

    Thread t1 = new Thread( ()->{
      try {
         res[0] = upper.mul(o);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    t1.start();
    Thread t2 = new Thread( ()->{
      try {
        res[1] = lower.mul(o);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    t2.start();
    t1.join();
    t2.join();

    res_value = res[0].value;
    res_value.addAll(res[1].value);

    res_index_column = res[0].index_column;
    res_index_column.addAll(res[1].index_column);

    res_ptr_row = res[0].ptr_row;
    res[1].ptr_row.remove(0);

    for (int k = 0 ; k <res[1].ptr_row.size() ; k++){
      res_ptr_row.add( res[1].ptr_row.get(k) + res[0].ptr_row.get(res[0].ptr_row.size()-1) );
    }

    SparseMatrix result = new SparseMatrix(res_value,res_ptr_row,res_index_column,o.width,this.height);
    return result;
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
      if (o instanceof DenseMatrix){
        return this.equals(((DenseMatrix) o).toCSR());
      }
      else if (o instanceof SparseMatrix){
        return this.equals((SparseMatrix)o);
      }
      else return false;

  }


}

class CSRvalue implements Comparable{
  public int colIndex;
  public double value;
  public int placeInRow;
  public CSRvalue(int placeInRow, double value, int colIndex){
    this.placeInRow = placeInRow;
    this.value = value;
    this.colIndex = colIndex;
  }
  @Override
  public int compareTo(Object o) {
    if (o instanceof CSRvalue){
      return this.placeInRow-((CSRvalue) o).placeInRow;
    }
    return 0;
  }

}
