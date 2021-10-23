package edu.spbu.matrix;

import edu.spbu.sort.IntSort;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatrixTest
{
  /**
   * ожидается 4 таких теста
   */
  @Test
  public void mulDD() throws Exception {
    long startTime = System.nanoTime();

    Matrix d1 = new DenseMatrix("./matrix_tests/DenseMatrix1.txt");
    Matrix d2 = new DenseMatrix("./matrix_tests/DenseMatrix2.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/dm1xdm2res.txt");
    Matrix res = d1.mul(d2);

    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));

    assertEquals(expected, res);
  }


  @Test
  public void mulDS() throws Exception {
    long startTime = System.nanoTime();

    Matrix d = new DenseMatrix("./matrix_tests/DenseMatrix3.txt");
    Matrix s = new DenseMatrix("./matrix_tests/SparseMatrix3.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/dm3xsm3_res.txt");
    Matrix res = d.mul(s);
    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));
    assertEquals(expected, res);
  }

  @Test
  public void mulSD() throws Exception {
    long startTime = System.nanoTime();

    Matrix d = new DenseMatrix("./matrix_tests/DenseMatrix3.txt");
    Matrix s = new DenseMatrix("./matrix_tests/SparseMatrix1.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/sm1xdm3_res.txt");
    Matrix res = s.mul(d);
    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));
    assertEquals(expected, res);
  }

  @Test
  public void mulSS() throws Exception {
    long startTime = System.nanoTime();

    Matrix s1 = new DenseMatrix("./matrix_tests/SparseMatrix1.txt");
    Matrix s2 = new DenseMatrix("./matrix_tests/SparseMatrix1.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/sm1xsm1_res.txt");
    Matrix res = s1.mul(s2);
    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));
    assertEquals(expected, res);
  }

  @Test
  public void dmulDD() throws Exception {
    long startTime = System.nanoTime();

    Matrix d1 = new DenseMatrix("./matrix_tests/DenseMatrix4.txt");
    Matrix d2 = new DenseMatrix("./matrix_tests/DenseMatrix5.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/dm4xdm5_res.txt");
    Matrix res = d1.dmul(d2);

    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));

    assertEquals(expected, res);
  }

  @Test
  public void dmulSS() throws Exception {
    long startTime = System.nanoTime();

    Matrix s1 = new DenseMatrix("./matrix_tests/SparseMatrix1.txt");
    Matrix s2 = new DenseMatrix("./matrix_tests/SparseMatrix1.txt");
    Matrix expected = new DenseMatrix("./matrix_tests/sm1xsm1_res.txt");
    Matrix res = s1.dmul(s2);
    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Execution time(ms) " + (estimatedTime/ 1000000));
    assertEquals(expected, res);
  }

}
