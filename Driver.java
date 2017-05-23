public class Driver {
	public static void main(String[] args)
	{
		Elevator e = new Elevator("up", 0);
		Clean c = new Clean(e);
		
		e.start();
		c.start();
		
		for (int i = 0; i < 20; i++)
		{
		Occupant occ = new Occupant(e, i);
		occ.start();
		}
		
	}
}
