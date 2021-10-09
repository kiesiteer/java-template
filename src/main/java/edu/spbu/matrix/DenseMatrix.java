package edu.spbu.matrix;




import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix
{
  public int height, width;
  public double[][] value;

  public DenseMatrix(double[][] value) {
    this.height = value.length;
    this.width = value[0].length;
    this.value = value;
  }

  /**
   * загружает матрицу из файла
   * @param fileName
   */
  public DenseMatrix(String fileName) {
    this.width = 0;
    this.height = 0;

    LinkedList<double[]> rows = new LinkedList<double[]>();
    try {
      Scanner sc = new Scanner(new FileReader(fileName));
      if (!sc.hasNextLine()) {
        throw new Exception("Пустой файл");
      }
      String line = sc.nextLine();
      double[] row = Arrays.stream(line.split(" ")).mapToDouble(Double::parseDouble).toArray();
      rows.add(row);
      this.width = row.length;


      while( sc.hasNextLine() ){
        line = sc.nextLine();
        row = Arrays.stream(line.split(" ")).mapToDouble(Double::parseDouble).toArray();
        rows.add(row);
      }
      this.height = rows.size();

    } catch (Exception e) {
      e.printStackTrace();
    }

    this.value = new double[this.height][this.width];
    for (int i = 0; i < this.height; i++) {
      this.value[i] = rows.get(i);
    }


  }


  private DenseMatrix mul(DenseMatrix o) throws Exception {
    if (this.width != o.height){throw new Exception("Не совпадают размеры матриц");}
    double[][] res = new double[this.height][o.width];

    for (int i = 0; i < this.height; i++) {
      for (int j = 0; j < o.width; j++) {
        res[i][j] = 0;
        for (int k = 0; k < this.width; k++){
          res[i][j] += this.value[i][k] * o.value[k][j];
        }
      }
    }
    return new DenseMatrix(res);
  }

  private SparseMatrix mul(SparseMatrix o) throws Exception{
    if (this.width != o.height) {throw new Exception("Не совпадают размеры матриц");}
    ArrayList<Integer> res_ptr_row = new ArrayList<>();
    ArrayList<Integer> res_index_column = new ArrayList<>();
    ArrayList<Double> res_value = new ArrayList<>();
    res_ptr_row.add(0);//запирающий элемент
    SparseMatrix ot = o.transposeCSR();

    for (int i = 0 ; i < this.height ; i++){
      int k = 0;
      for (int i2 = 0 ; i2 < ot.height ; i2++) { //для каждого столбца правой (строки правой транспонированной)
        double x = 0;
        for (int j = 0 ; j < (ot.ptr_row.get(i2+1) - ot.ptr_row.get(i2)) ; j++){
          x += value[i][ot.index_column.get(k+j)] * ot.value.get(k+j);
        }
        if (x != 0){
          res_value.add(x);
          res_index_column.add(i2);
        }
        res_ptr_row.add(res_value.size());
        k += (ot.ptr_row.get(i2+1) - ot.ptr_row.get(i2));
      }
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
  @Override public Matrix dmul(Matrix o) {
    return null;
  }

  private boolean equals(DenseMatrix o) {
    if (this == o) return true;
    if (this.height != o.height && this.width != o.width) return false;
    else {
      for (int i = 0; i < this.height; i++)
        for (int j =0; j < this.width; j++)
          if (this.value[i][j] != o.value[i][j]) return false;
      return true;
    }
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    if (o instanceof DenseMatrix){
      return this.mul((DenseMatrix) o);
    }
    else if (o instanceof SparseMatrix){
      return this.mul((SparseMatrix) o);
    }
    else return null;
  }

}
