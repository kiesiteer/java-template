package edu.spbu.matrix;


import com.sun.jmx.remote.internal.ArrayQueue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      String line;
      if ( (line = br.readLine()) == null) {
        throw new Exception("Пустой файл");
      }
      double[] row = Arrays.stream(line.split(" ")).mapToDouble(Double::parseDouble).toArray();
      rows.add(row);
      this.width = row.length;


      while( (line = br.readLine()) != null ){
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

  public DenseMatrix mul(DenseMatrix o) throws Exception {
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
