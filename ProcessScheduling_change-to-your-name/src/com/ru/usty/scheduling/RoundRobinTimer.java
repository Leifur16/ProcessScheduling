package com.ru.usty.scheduling;


public class RoundRobinTimer implements Runnable{
	
	private int quantum;
	
	
	// Constructor
	public RoundRobinTimer(int quantum) {
		this.quantum = quantum;
		System.out.println("new runnable made");
	}
	
	
	@Override
	public void run() {
		while(true) {
			if(Scheduler.rrMayDie) {
				System.out.println("terminating quantum!!!!!!!!!!!!");
				return;
			}
			
			try {
				Thread.sleep(quantum);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			

			
			Scheduler.nextQueue();
			
			
		}
	}
}


