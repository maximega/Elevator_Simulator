import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Elevator extends Thread{
	public volatile Semaphore Mutex = new Semaphore(1); //ensures mutual exclusion 
	public volatile Semaphore capacity = new Semaphore(3); //ensures that no more than 3 occupants can enter the elevator at once
	public volatile Semaphore called = new Semaphore(0); //ensures that the elevator is stationary until called
	public volatile int current_floor = 0; //start floor of the elevator
	public volatile String direction = "up"; //initial direction of the elevator
	public volatile Semaphore janitor = new Semaphore(1); 
	public volatile Semaphore request_janitor = new Semaphore(0);
	public volatile int floor_counter; //global counter for number of floors traveled
	public volatile ArrayList <Tuple> requests = new ArrayList<Tuple>(); //ArrayList of tuples that correspond to requests made by the occupants
	
	public Elevator(String direction, int current_floor)
	{
		super();
		this.direction = direction;
		this.current_floor = current_floor;
	}
	public void add_elevator(Tuple tup)
	{
		this.requests.add(tup);
	}
	public void rem_elevator(Tuple tup)
	{
		this.requests.remove(tup);
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
	public void  runAll() throws InterruptedException
	{
			//once the elevator has traveled at least 80 floors, reuqests a cleaning from the janitor
			if(this.floor_counter >= 80)
			{
				this.request_janitor.release();
				this.floor_counter = 0;
			}
			this.called.acquire(); //elevator waits to be called
			Tuple var;
			//if direction is up, find closest request moving up
			//if there are no requests going up, then the elevator changes its direction and looks for the closest request moving down
			if(this.direction.equals("up"))
			{
				
				this.Mutex.acquire();
				var = get_closestUp();
				this.Mutex.release();
					if (var == null)
					{
						System.out.println("Elevator's direction has changed");
						this.direction = "down";
						this.Mutex.acquire();
						var = get_closestDown();
						this.Mutex.release();
					}
			}
			//if direction is down, find closest request moving down
			//if there are no requests going down, then the elevator changes its direction and looks for the closest request moving up
			else
			{
				this.Mutex.acquire();
				var = get_closestDown();
				this.Mutex.release();
					if (var == null)
					{
						System.out.println("Elevator's direction has changed");
						this.direction = "up";
						this.Mutex.acquire();
						var = get_closestUp();
						this.Mutex.release();
					}
			}
			int temp = var.getFLR(); //set 'temp' to whatever 'var' is set to
			this.floor_counter += (Math.abs(this.current_floor - temp));
			Thread.sleep(Math.abs(this.current_floor - temp) * 5);//sleep for a certain amount of time
			this.current_floor = temp;
			//get1() and get2() defined in the tuple class
			var.get1().release(); //signals that the elevator has arrived
			
			var.get2().acquire(); //waits for the occupant to eneter/exit the elevator
			
			this.Mutex.acquire();
			this.rem_elevator(var); //removes the most recently serviced occupant from the array of requests.
			this.Mutex.release();
	}
	public Tuple get_closestUp()
	{
		Tuple n = (Tuple)null;
		int distance = 9999;
		int closest = 9999;
		for (int i = 0; i < this.requests.size(); i++)
		{
			int flr = this.requests.get(i).getFLR();
			if (flr >= this.current_floor) {
				distance = flr - this.current_floor;
				if (distance < closest) {
					n = this.requests.get(i);
					closest = distance;
				}
			}	
		}
		return n;
	}
	public Tuple get_closestDown()
	{
		Tuple n = (Tuple)null;
		int distance = 9999;
		int closest = 9999;
		for (int i = 0; i < this.requests.size(); i++)
		{
			int flr = this.requests.get(i).getFLR();
			if (flr <= this.current_floor) {
				distance = this.current_floor - flr;
				if (distance < closest) {
					n = this.requests.get(i);
					closest = distance;
				}
			}
		}
		return n;
	}
}