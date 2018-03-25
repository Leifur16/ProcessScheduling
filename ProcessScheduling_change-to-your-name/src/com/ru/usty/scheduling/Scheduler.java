package com.ru.usty.scheduling;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {
	
	/**
	 * Add any objects and variables here (if needed)
	 */
	static ProcessExecution processExecution;
	Policy policy;
	int quantum;
	public static boolean rrMayDie = false;
	public static Queue<Integer> processQueue;
	private static Thread thread = null;
	
	static Semaphore switchToProcessMutex = new Semaphore(1);


	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		Scheduler.processExecution = processExecution;
		processQueue = new LinkedList<Integer>();
	}

	public static void nextQueue() {
		if(processQueue.size() > 1) {
			try {
				switchToProcessMutex.acquire();
					int temp = processQueue.remove();
					processQueue.add(temp);
					processExecution.switchToProcess(processQueue.element());
				switchToProcessMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if(processQueue.size() == 1) {
			try {
				switchToProcessMutex.acquire();
					processExecution.switchToProcess(processQueue.element());
				switchToProcessMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void startScheduling(Policy policy, int quantum) {
		this.policy = policy;
		this.quantum = quantum;

		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			processQueue = new LinkedList<Integer>();
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			
			rrMayDie = true;
			if(thread != null) {
				if(thread.isAlive()) {
					try {
						System.out.println("==================== thread was removed  IN BEGINNING =====================");
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			rrMayDie = false;
					
			processQueue = new LinkedList<Integer>();
	
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();
			
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			break;
		}

		/**
		 * Add general scheduling or initialization code here (if needed)
		 */	
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processAdded(int processID) {
		ProcessInfo info = processExecution.getProcessInfo(processID);
		
		System.out.println("PROCESS ID: " + processID);
		System.out.println("total time: " + info.totalServiceTime);
		System.out.println("Execution time: " + info.elapsedExecutionTime);
		System.out.println("waiting time: " + info.elapsedWaitingTime);

		
		switch(policy) {
		case FCFS:	//First-come-first-served
			if(processQueue.size() == 0) {
				System.out.println("hello?");
				processExecution.switchToProcess(processID);
			}
			processQueue.add(processID);
			
			break;
		case RR:	//Round robin
			
			//processQueue.add(processID);
			try {
				switchToProcessMutex.acquire();
					processQueue.add(processID);
				switchToProcessMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {
		
		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Process finished");
			processQueue.remove();
			if(processQueue.size() > 0) {
				processExecution.switchToProcess(processQueue.element());
			}
			
			break;
		case RR:	//Round robin
			System.out.println("Process finished " + processID);
			System.out.println("PROCESS DIED " + processID);
			
			
			try {
				switchToProcessMutex.acquire();
					processQueue.remove();
				switchToProcessMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			

			break;
		default:
			break;
		}
		
		

	}
}
