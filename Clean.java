import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/*
This class creates a janitor that enters and cleans the elevator once the elevator has traveled 80 or more floors.
The janitor cannot start cleaning until all occupants have left the elevator.
No new occupants can eneter the elevator until the elevator is cleaned and the janitor leaves
*/

public class Clean extends Thread{
	//the semaphores below have the same funcionality as the semaphores int the Occupant class
	public volatile Elevator elevator; 
	public volatile Semaphore arr = new Semaphore(0);
	public volatile Semaphore enter = new Semaphore(0);
	public volatile Semaphore x_it = new Semaphore(0);
	
	public Clean(Elevator e){
		super();
		elevator = e;
	}
	
	public void run()
	{
		while(true)
		{
		try{
			this.runAll();
			} catch(InterruptedException e){};
		}
	}
	public void runAll() throws InterruptedException
	{
		while(true)
		{
			elevator.request_janitor.acquire(); //waits to be requested
			elevator.janitor.acquire(); //aquires the janitor semaphore, to ensure no new occupant enter the elevator
			elevator.capacity.acquire(); 
			
			elevator.Mutex.acquire();
			Tuple call_vator = new Tuple(1, this.arr, this.enter); //janitor always enters on the first floor
			System.out.println("Janitor is coming in on floor 1"); 
			elevator.add_elevator(call_vator); //add tuple to the pending service requests
			elevator.Mutex.release();
			
			elevator.called.release(); //call elevator from source floor
			
			this.arr.acquire(); //wait until the elevator arrives
		
			this.enter.release(); //eneter the elevator
			System.err.println("janitor arrives");
			
			elevator.capacity.acquire();
			elevator.capacity.acquire();
			//simulates that the elevator is full, since janitor is the only person allowed in the elevator
			
			elevator.Mutex.acquire();
			Tuple leave_vator = new Tuple(1, this.arr, this.x_it);
			elevator.add_elevator(leave_vator); //add tuple to pending exit requests
			elevator.Mutex.release();
			
			elevator.called.release();
			
			this.arr.acquire(); //wait for elevator arrival 
			
			System.err.println("I am cleaningggggggg");
			
			Thread.sleep(2000); //cleans for this amount of time
			this.x_it.release(); // exit the elevator
			elevator.janitor.release(); //signal that the janitor is finished cleaning
			
			for (int k = 0; k < 3; k++)
			{
				elevator.capacity.release();
			}
			//make sure that the elevator is empty 
		}
	}
}
