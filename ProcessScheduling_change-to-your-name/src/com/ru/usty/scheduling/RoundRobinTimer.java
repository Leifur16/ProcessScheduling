package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;

public class RoundRobinTimer implements Runnable{
	
	private int quantum;
	ProcessExecution processExecution;
	
	// Constructor
	public RoundRobinTimer(int quantum) {
		this.quantum = quantum;
	}
	
	@Override
	public void run() {
		System.out.println("============================= I live ==========================================");
		
		while(true) {
			if(Scheduler.rrMayDie) {
				System.out.println("============================= I diead ==========================================");
				return;
			}
			
			try {
				Thread.sleep(quantum);
				//System.out.println("This thing is running ");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("before currTime is taken");
			Scheduler.currTime = System.currentTimeMillis();
			System.out.println("after currTime is taken");
			while(Scheduler.currTime - Scheduler.startTime < quantum) {
				System.out.println("=================== FINISHING MY TIME ===========================");
				try {
					Thread.sleep(quantum - (Scheduler.currTime - Scheduler.startTime));
					Scheduler.currTime = System.currentTimeMillis();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Scheduler.nextQueue();
		}
	}
}


