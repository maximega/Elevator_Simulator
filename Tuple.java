import java.util.concurrent.Semaphore;

public class Tuple { 
		  public final int flr; 
		  public final Semaphore sem_1; 
		  public final Semaphore sem_2; 
		  public Tuple(int flr, Semaphore sem_1, Semaphore sem_2) { 
		    this.flr = flr; 
		    this.sem_1 = sem_1; 
		    this.sem_2 = sem_2; 
		  } 
		 public int getFLR()
		 {
			 return this.flr;
		 }
		 public Semaphore get1()
		 {
			 return this.sem_1;
		 }
		 public Semaphore get2()
		 {
			 return this.sem_2;
		 }
	} 