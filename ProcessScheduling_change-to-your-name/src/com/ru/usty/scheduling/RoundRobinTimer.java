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
		
		while(true) {
			if(Scheduler.rrMayDie) {
				return;
			}
			
			try {
				Thread.sleep(quantum);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			Scheduler.currTime = System.currentTimeMillis();
			
			while((Scheduler.currTime - Scheduler.startTime) < quantum) {
				try {
					// + 10 not to fix conflict errors but to fix error an error where
					// if process is started to close to startTime being taken threads 
					// sometime won't start running even though it has definitely been 
					// switched to
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


