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
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			System.out.println("before currTime is taken");
			Scheduler.currTime = System.currentTimeMillis();
			System.out.println("after currTime is taken");
			
			System.out.println("Time diff" + (System.currentTimeMillis() - Scheduler.startTime) + "<" + quantum);
			
			while((Scheduler.currTime - Scheduler.startTime) < quantum) {
				System.out.println("=================== FINISHING MY TIME ===========================");
				try {
					// + 10 not to fix conflict errors but to fix error with given code
					// if process is started to close to startTime being taken threads sometime wont
					// start running even though it has definitely been switched to
					Thread.sleep(quantum - (Scheduler.currTime - Scheduler.startTime) + 10); 
					Scheduler.currTime = System.currentTimeMillis();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Scheduler.switchMutexParent.acquire();
				Scheduler.nextQueue();
				Scheduler.switchMutexParent.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}


