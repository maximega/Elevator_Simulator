import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Occupant extends Thread{
	public volatile Semaphore arr = new Semaphore(0); //semaphore to ensure occupant waits on elevator arrival
	public volatile Semaphore enter = new Semaphore(0); //semephore to ensure that the elevator waits for an occupant to enter before it continues service
	public volatile Semaphore x_it = new Semaphore(0); //semephore to ensure occupant has safely exited the elevator
	public volatile Elevator elevator; 
	public volatile int start_floor;
	public volatile int dest_floor;
	public volatile int id;
	
	public Occupant(Elevator e, int id)
	{
		super();
		this.elevator = e;
		this.start_floor = ThreadLocalRandom.current().nextInt(5); //building only has up to 5 floors
		this.dest_floor = ThreadLocalRandom.current().nextInt(5);
		//chooses a new start and destination floor if they are the same value
		while(this.start_floor == this.dest_floor)
		{
			this.start_floor = ThreadLocalRandom.current().nextInt(5);
			this.dest_floor = ThreadLocalRandom.current().nextInt(5);
		}	
		this.id = id;
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
		elevator.janitor.acquire(); //if a janitor is in the elevator, all occupants must wait until he leaves
		elevator.janitor.release(); 
		elevator.capacity.acquire(); //check to see if there is room in the elevator
		
		elevator.Mutex.acquire();
		Tuple entrance = new Tuple(this.start_floor, this.arr, this.enter);
		//above statement utilizes tuple class
		System.out.println("Occupant " + this.id + " wants to go from " + this.start_floor + " to " + this.dest_floor);
		elevator.add_elevator(entrance); //adds newly created tuple to a tuple array of pending service requests
		elevator.Mutex.release();
		
		elevator.called.release(); //signal that you have called the elevator
		System.out.println("Occupant " + this.id + "has called the elevator");
		this.arr.acquire(); //wait until the elevator arrives
		
		System.err.println("HELLO ELEVATOR HAS ARRIVED");
		
		elevator.Mutex.acquire();
		Tuple leaving = new Tuple(this.dest_floor, this.arr, this.x_it); //creates tuple with the intention of exiting the elevator
		elevator.add_elevator(leaving); //adds newly created tuple to the pending exit requests
		elevator.Mutex.release();
		
		System.err.println("Occupant " + this.id + " is entering right now on floor " + this.start_floor);

		this.enter.release(); //occupant enters the elevator
		
		elevator.called.release(); //signals that the occupant has enetered their desired destination 
		
		this.arr.acquire(); //elevator has arrived at the destination floor
		
		this.x_it.release(); // signal that the occupant has exited the elevator
		
		System.err.println("Occupant " + this.id + " is leaving right now on floor " + this.dest_floor);
		elevator.capacity.release(); // signal that it is now ok for another occupant to eneter
		
		//sleep for a predetermined amount of time
		double num = Math.random();
		long val = (long)(Math.log(1-num)/ -(this.id * 500 + 1));
		Thread.sleep(val);
	}
}
