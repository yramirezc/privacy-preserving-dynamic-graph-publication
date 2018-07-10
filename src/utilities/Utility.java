package utilities;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.lang.Math;

public class Utility {
	
/// ********************************* Random Permutation  ************************************ ///
public static void randomPermutation(int n, int perm[]){
		 int i,j,k;
		 long seed=1;
		 Random ran=new Random(seed);
		 
		 for (i=0; i<n; i++)
		    perm[i] = i;
		 
		 for (i=0; i<n; i++) {
		    j = (int)(i + ran.nextDouble() * (n- i));
		    k = perm[i];
		    perm[i] = perm[j];
		    perm[j] = k;
		  }
}
public static Set<Integer> intersection(Set<Integer> A , Set<Integer> B){
		Set <Integer> C =new HashSet<Integer> ();
		if(A.size() < B.size()){
			for(int n:A)
				if(B.contains(n)) C.add(n); else continue;}
		else{
			for(int n:B)
				if(A.contains(n)) C.add(n); else continue;}

return C;
}


public static Set<String> intersection_str(Set<String> A , Set<String> B){
	Set <String> C =new HashSet<String> ();
	if(A.size() < B.size()){
		for(String s:A)
			if(B.contains(s)) C.add(s); else continue;}
	else{
		for(String s :B)
			if(A.contains(s)) C.add(s); else continue;}

return C;
}

public static Set<Integer> subtraction(Set<Integer> A , Set<Integer> B){
		Set <Integer> C =new HashSet<Integer> ();
		//Set <Integer> D =new HashSet<Integer> ();	
			for(int n:A)
				if(!B.contains(n)) C.add(n); else continue;
	return C;
	}
public static Set<Integer> SetThePair(int node1,int node2){
		Set<Integer> pair=new HashSet<Integer>();
		pair.add(node1);
		pair.add(node2);
	return pair;
	}
public static double k_l_divergenz(double x,double y){
	   double k_l_d= 0 ;
	   if(x==0 || y==0) return 0;
       k_l_d=x * (Math.log(x) - Math.log(y));
return k_l_d;
}
public static double k_l_metric(double[] X ,double[] Y){
	   double distance=0;
	   int n=X.length;
	   int m=Y.length;
	   
	   for(int i=0;i<Math.max(n, m);++i)
		   if(X[i]!=0 && Y[i]!=0) distance+=k_l_divergenz(X[i], Y[i]);
		   	   
return distance;	   
}

public static double euclid_distance(double x,double y){
	   double distance=0;
	   
	   distance= Math.sqrt(Math.pow(x-y, 2));
return distance;
}
public static double euclid_metric(double[] X,double[] Y){
	   double distance=0;
	   int n=X.length;
	   int m=Y.length;
	   
	   for(int i=0;i<Math.max(n, m);++i)
		   if(X[i]!=0 && Y[i]!=0) distance+=euclid_distance(X[i], Y[i]);
	   
return distance;	   
}

public static double innerProduct(double[] X,double[] Y){
	          double d=0;
	          int n=Math.min(X.length, Y.length);
	          for(int i=0;i<n;++n)
	        	  d+=X[i]*Y[i];	        	  
	          
return d;	      
}

public static double cosine(double[] X,double[] Y){
			  double c;
			  c=Math.sqrt(innerProduct(X, Y)/(innerProduct(X, X)*innerProduct(Y, Y)));
return c;			  
}
/* End of Class */ }