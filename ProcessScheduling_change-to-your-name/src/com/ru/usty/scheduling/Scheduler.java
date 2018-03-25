package com.ru.usty.scheduling;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {

	static ProcessExecution processExecution;
	Policy policy;
	int quantum;
	public static boolean rrMayDie = false;
	
	public static Queue<Integer> processQueue;
	private Thread thread = null;
	
	/**
	 * Add any objects and variables here (if needed)
	 */
	
	static Semaphore switchMutex = null; 

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;

		/**
		 * Add general initialization code here (if needed)
		 */
		processQueue = new LinkedList<Integer>();
	}
	
	public static void nextQueue() {
		
		try {
			switchMutex.acquire();
				if(processQueue.size() > 1) {
					int temp = processQueue.remove();
					processQueue.add(temp);
					processExecution.switchToProcess(processQueue.element());
				}
				else if(processQueue.size() == 1) {
					processExecution.switchToProcess(processQueue.element());
				}
			switchMutex.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void startScheduling(Policy policy, int quantum) {

		this.policy = policy;
		this.quantum = quantum;
		System.out.println("policy: " + policy);
		System.out.println("quantum: " + quantum);
		

		/**
		 * Add general initialization code here (if needed)
		 */
		
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

		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			processQueue = new LinkedList<Integer>();
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			processQueue = null;
			processQueue = new LinkedList<Integer>();
			switchMutex = new Semaphore(1);
			
			System.out.println("==================== thread was started  IN BEGINNING =====================");
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();

			break;
		case SPN:	//Shortest process next
			//rrMayDie = true;

			System.out.println("Starting new scheduling task: Shortest process next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
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

		/**
		 * Add scheduling code here
		 */
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
			
			try {
				switchMutex.acquire();
					processQueue.add(processID);	
				switchMutex.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
		
		System.out.println("Process finished");
		/**
		 * Add scheduling code here
		 */
		
		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Process finished");
			processQueue.remove();
			if(processQueue.size() > 0) {
				processExecution.switchToProcess(processQueue.element());
			}
			break;
		case RR:	//Round robin
			
			try {
				switchMutex.acquire();
					processQueue.remove();
				switchMutex.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		default:
			break;
		}	
	}
}
