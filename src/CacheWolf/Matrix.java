    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;

public final class Matrix{
	int iDF = 0;
	
	public double matrix[][] = new double[0][0];
	
	public Matrix (int rows, int cols){
		matrix = new double[rows][cols];
	}
	
	public Matrix (Matrix srcMatrix){
		matrix = new double[srcMatrix.matrix.length][srcMatrix.matrix[0].length];
		for (int i = 0; i < srcMatrix.matrix.length; i++)
			for (int j = 0; j < srcMatrix.matrix[i].length; j++)
				matrix[i][j] = srcMatrix.matrix[i][j];
	}
	
	/**
	*	Method to multiply this matrix with another matrix.
	*	The result is stored in this matrix!
	*/
	public void Multiply(Matrix srcMatrix){
		double m[][] = new double[matrix.length][srcMatrix.matrix[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++){
				m[i][j] = calculateRowColumnProduct(matrix,i,srcMatrix.matrix,j);
			}
		
		matrix = m;
	}
	
	/**
	*	Method to calculate the row column product of two matrices.
	*	Is used by the Multiply method.
	*/
	private double calculateRowColumnProduct(double[][] A, int row, double[][] B, int col){
		double product = 0;
		for(int i = 0; i < A[row].length; i++){
			product +=A[row][i]*B[i][col];
		}
		return product;
	}
	
	public void MultiplyByScalar (double f) {
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[0].length; j++)
				matrix[i][j] = matrix[i][j] * f;
	}
	
	public void add (Matrix a) {
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[0].length; j++)
				matrix[i][j] = matrix[i][j] + a.matrix[i][j];
	}
	
	/**
	*	Method to transpose a matrix
	*	example:	| 1 2 |
	*			| 3 4 |
	*			| 5 6 |
	*	would become:	|1 3 5 |
	*			|2 4 6 |
	*/
	public void Transpose(){
		
		double m[][] = new double[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				m[j][i] = matrix[i][j];
			
		matrix = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++)
				matrix[i][j] = m[i][j];
	}
	
	/**
	*	private version of the Transpose method.
	*	used internally in this class
	*/
	private double[][] Transpose2(double[][] a) {
		
		double m[][] = new double[a[0].length][a.length];

		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				m[j][i] = a[i][j];
		return m;
	}
	
	/**
	*	Method to display the contents of a matrix.
	*/
	public void DumpMatrix(){
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				Global.getPref().log("[ "+i+ " " + j + " ] " + matrix[i][j]);
	}
	
	/**
	*	Method used to help calculate determinate
	*/
	private double[][] UpperTriangle(double[][] m) {
		double f1 = 0;
		double temp = 0;
		int tms = m.length; // get This Matrix Size (could be smaller than
							// global)
		int v = 1;
		iDF = 1;

		for (int col = 0; col < tms - 1; col++) {
			for (int row = col + 1; row < tms; row++) {
				v = 1;
				outahere: while (m[col][col] == 0) // check if 0 in diagonal
				{ // if so switch until not
					if (col + v >= tms) // check if switched all rows
					{
						iDF = 0;
						break outahere;
					} else {
						for (int c = 0; c < tms; c++) {
							temp = m[col][c];
							m[col][c] = m[col + v][c]; // switch rows
							m[col + v][c] = temp;
						}
						v++; // count row switchs
						iDF = iDF * -1; // each switch changes determinant
										// factor
					}
				}
				if (m[col][col] != 0) {
					try {
						f1 = (-1) * m[row][col] / m[col][col];
						for (int i = col; i < tms; i++) {
							m[row][i] = f1 * m[col][i] + m[row][i];
						}
					} catch (Exception e) {
						Global.getPref().log("Still Here!!!");
					}
				}
			}
		}
		return m;
	}
	
	/**
	*	Method to calculate the determinate of a matrix
	*/
	public double Determinant(double[][] pMatrix) {
		int tms = pMatrix.length;
		double det = 1;
		pMatrix = UpperTriangle(pMatrix);
		for (int i = 0; i < tms; i++) {
			det = det * pMatrix[i][i];
		} // multiply down diagonal
		det = det * iDF; // adjust w/ determinant factor
		return det;
	}
	
	
	
	/**
	*	Method to calculate the inverse of this matrix.
	*	The result is stored in this matrix!
	*/
	public void Inverse() {
		// Formula used to Calculate Inverse:
		// inv(A) = 1/det(A) * adj(A)
		
		int tms = matrix.length;

		double m[][] = new double[tms][tms];
		double mm[][] = Adjoint(matrix);

		double det = Determinant(matrix);
		double dd = 0;

		if (det == 0) {
			Global.getPref().log("Determinant Equals 0, Not Invertible.");
		} else {
			dd = 1 / det;
		}
		for (int i = 0; i < tms; i++)
			for (int j = 0; j < tms; j++) {
				m[i][j] = dd * mm[i][j];
			}
			
		//Store back results
		matrix = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++)
				matrix[i][j] = m[i][j];
	}
	
	/**
	*	Method to calculate the adjoint of a matrix.
	*	Required to calculate the inverse of a matrix.
	*/
	private double[][] Adjoint(double[][] a) {
		int tms = a.length;

		double m[][] = new double[tms][tms];

		int ii, jj, ia, ja;
		double det;

		for (int i = 0; i < tms; i++)
			for (int j = 0; j < tms; j++) {
				ia = ja = 0;
				double ap[][] = new double[tms - 1][tms - 1];
				for (ii = 0; ii < tms; ii++) {
					for (jj = 0; jj < tms; jj++) {
						if ((ii != i) && (jj != j)) {
							ap[ia][ja] = a[ii][jj];
							ja++;
						}
					}
					if ((ii != i) && (jj != j)) {
						ia++;
					}
					ja = 0;
				}
				det = Determinant(ap);
				m[i][j] = java.lang.Math.pow(-1, i + j) * det;
			}
		m = Transpose2(m);
		return m;
	}
	
	/**
	*	"Old" helper method used by some other classes in cachewolf.
	*/
	//  No reason for deprecation, so I removed it. Or is there a better substitution for this
	//  method?
	//	@deprecated Do not use when coding new classes!
	public static double dot(double p1, double p2, double q1, double q2, double x1, double x2){
		double dt,AB0,AB1,BC0,BC1 = 0;
		AB0 = q1 - p1;
		AB1 = q2 - p2;
		BC0 = x1-q1;
		BC1 = x2-q2;
		dt = AB0 * BC0 + AB1 * BC1;
		return dt;
	}
	
	/**
	*	"Old" helper method used by some other classes in cachewolf.
	*/
	//  No reason for deprecation, so I removed it. Or is there a better substitution for this
	//  method?
	//	@deprecated Do not use when coding new classes!
	public static double cross(double p1, double p2, double q1, double q2, double x1, double x2){
		double cr,AB0,AB1,AC0,AC1 = 0;
		AB0 = q1 - p1;
		AB1 = q2 - p2;
		AC0 = x1-p1;
		AC1 = x2-p2;
		cr= AB0 * AC1 - AB1 * AC0;
		return cr;
	}
	
	/**
	*	"Old" helper method used by some other classes in cachewolf.
	*/
	//  No reason for deprecation, so I removed it. Or is there a better substitution for this
	//  method?
	//	@deprecated Do not use when coding new classes!
	public static double dist(double p1, double p2, double q1, double q2){
		double d1, d2,dt = 0;
		d1 = p1 - q1;
		d2 = p2 - q2;
		dt = d1 * d1 + d2 * d2;
		dt = java.lang.Math.sqrt(dt);
		return dt;
	}
}