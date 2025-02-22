package edu.spbu.matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
  if (this.width != o.height){throw new Exception("Не совпадают размеры матриц");}
  int threadCount = Runtime.getRuntime().availableProcessors();
  int submatrixHeight = this.height / threadCount;
  int residueHeight = this.height % threadCount;
  int submatricesCount = threadCount;
  if (residueHeight != 0) submatricesCount++;


  Future [] tasks = new Future[submatricesCount];

  ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
  for(int i = 0 ; i < threadCount ; i++){

    int fi = i;
    tasks[i]=executorService.submit(()->{
      //выдрать подматрицу из this
      ArrayList<Integer> submatrix_ptr_row = new ArrayList<Integer> (this.ptr_row.subList(fi * submatrixHeight + 1,(fi + 1) * submatrixHeight + 1));
      ArrayList<Integer> submatrix_index_column = new ArrayList<Integer>( this.index_column.subList( this.ptr_row.get(fi * submatrixHeight) , this.ptr_row.get((fi+1) * submatrixHeight) ) );
      ArrayList<Double> submatrix_value = new ArrayList<Double>( this.value.subList( this.ptr_row.get(fi * submatrixHeight) , this.ptr_row.get((fi+1) * submatrixHeight) ) );

      //вычесть из submatrix_ptr_row и добавить 0 в начало
      for (int k = 0 ; k < submatrix_ptr_row.size() ; k++){
        submatrix_ptr_row.add(k , submatrix_ptr_row.get(k) - this.ptr_row.get(fi * submatrixHeight) );
        submatrix_ptr_row.remove(k+1);
      }
      submatrix_ptr_row.add(0, 0);

      SparseMatrix submatrix = new SparseMatrix(submatrix_value,submatrix_ptr_row,submatrix_index_column,this.width,submatrixHeight);
      //помножить подматрицу на o, получить resSubmatrix



      try {
        SparseMatrix resSubmatrix = submatrix.mul(o);
        return resSubmatrix;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }
  if (residueHeight != 0){
    tasks[threadCount]=executorService.submit(()->{
      ArrayList<Integer> submatrix_ptr_row = new ArrayList<Integer> (this.ptr_row.subList(threadCount * submatrixHeight + 1, this.ptr_row.size() ));
      ArrayList<Integer> submatrix_index_column = new ArrayList<Integer>( this.index_column.subList( this.ptr_row.get(threadCount * submatrixHeight) , this.index_column.size() ) );
      ArrayList<Double> submatrix_value = new ArrayList<Double>( this.value.subList( this.ptr_row.get(threadCount * submatrixHeight) , this.value.size()) );
      for (int k = 0 ; k <submatrix_ptr_row.size() ; k++){
        submatrix_ptr_row.add(k , submatrix_ptr_row.get(k) - this.ptr_row.get(threadCount * submatrixHeight) );
        submatrix_ptr_row.remove(k+1);
      }
      submatrix_ptr_row.add(0, 0);
      SparseMatrix submatrix = new SparseMatrix(submatrix_value,submatrix_ptr_row,submatrix_index_column,this.width,residueHeight);

      try {
        SparseMatrix resSubmatrix = submatrix.mul(o);
        return resSubmatrix;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  ArrayList<Integer> res_ptr_row = new ArrayList<>();
  ArrayList<Integer> res_index_column = new ArrayList<>();
  ArrayList<Double> res_value = new ArrayList<>();
  res_ptr_row.add(0);


  for (int i = 0 ; i < submatricesCount ; i++){
    SparseMatrix cur = (SparseMatrix) tasks[i].get();
    cur.ptr_row.remove(0);
    cur.ptr_row.replaceAll(x -> x + res_ptr_row.get(res_ptr_row.size()-1));
    res_ptr_row.addAll(cur.ptr_row);
    res_index_column.addAll(cur.index_column);
    res_value.addAll(cur.value);
  }

  executorService.shutdown();
  SparseMatrix result = new SparseMatrix(res_value,res_ptr_row,res_index_column,o.width,this.height);
  return result;
}



  private boolean equals(SparseMatrix o) {
    double eps = 0.001;
    if (this == o) return true;
    if (this.height != o.height && this.width != o.width) return false;
    if ((this.index_column.equals(o.index_column) && this.ptr_row.equals(o.ptr_row)) == false) return false;
    for (int i = 0 ; i < this.value.size() ;i++){
      if (Math.abs(this.value.get(i) - o.value.get(i)) > eps) return false;
    }
    return true;
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
