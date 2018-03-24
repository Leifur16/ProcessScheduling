package com.ru.usty.scheduling;

import java.util.LinkedList;
import java.util.Queue;

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
		if(processQueue.size() != 0) {
			int tmp = processQueue.element();
			processQueue.remove();
			processQueue.add(tmp);
			processExecution.switchToProcess(processQueue.element());
			//System.out.println("went here now");
		}
		else {
			System.out.println("ERROR");
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
			
				
			
			processQueue = new LinkedList<Integer>();
			
			//thread = new Thread(new RoundRobinTimer(quantum));
			//thread.start();


			break;
		case SPN:	//Shortest process next
			rrMayDie = true;

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
		
		//processExecution.switchToProcess(processID);
		
		
		
		switch(policy) {
		case FCFS:	//First-come-first-served
			if(processQueue.size() == 0) {
				System.out.println("hello?");
				processExecution.switchToProcess(processID);
			}
			processQueue.add(processID);
			
			break;
		case RR:	//Round robin
			
			
			
			if(processQueue.size() == 0) {
				rrMayDie = false;
				thread = new Thread(new RoundRobinTimer(quantum));				
				thread.start();
			}
			
			processQueue.add(processID);

				
				
			
			//System.out.println("Process Queue size: " + processQueue.size());
			
			
			break;
		default:
			break;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {

		/**
		 * Add scheduling code here
		 */
		//ProcessInfo info = processExecution.getProcessInfo(processID);
		
		
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
			System.out.println("QUEUE SIZE " + processQueue.size());
			
			rrMayDie = true;
			System.out.println("PROCESS DIED " + processID);
			processQueue.remove();
			
			
			if(processQueue.size() != 0) {
				processExecution.switchToProcess(processQueue.element());
				
				rrMayDie = false;
				thread = new Thread(new RoundRobinTimer(quantum));
				thread.start();
				
				
			}else {
				System.out.println("END oF THE LINE");
			}
			break;
		default:
			break;
		}
		
		

	}
}
