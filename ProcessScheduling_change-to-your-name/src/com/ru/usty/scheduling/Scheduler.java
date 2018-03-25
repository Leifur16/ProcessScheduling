package com.ru.usty.scheduling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import com.badlogic.gdx.utils.Timer;
import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessHandler;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {

	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	public static Thread timer;
	ProcessInfo info;
	SPNSchedule schedule;
	
	Queue<Integer> processQueue;
	PriorityQueue<SPNSchedule> priorityProcessQueue;

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
			
			timer = new Thread();
			timer.start();
			processQueue = new LinkedList<Integer>();
			
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			priorityProcessQueue = new PriorityQueue<SPNSchedule>();
			schedule = new SPNSchedule();
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
		info = processExecution.getProcessInfo(processID);
		
		/*
		System.out.println("total time: " + info.totalServiceTime);
		System.out.println("Execution time: " + info.elapsedExecutionTime);
		System.out.println("waiting time: " + info.elapsedWaitingTime);
		*/
		//processExecution.switchToProcess(processID);
		switch(policy) {
		
		case FCFS:
			if(processQueue.size() == 0) {
				processExecution.switchToProcess(processID);
			}
			processQueue.add(processID);
			break;
		case RR:
			try {
				
				timer.sleep(quantum - info.elapsedExecutionTime);
				//info.elapsedExecutionTime = quantum;
				if(!processQueue.contains(processID) ) {
					processQueue.add(processID);	
				}
				
				processExecution.switchToProcess(processID);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case SPN:
			info = processExecution.getProcessInfo(processID);
			SPNSchedule spn = new SPNSchedule(processID, info.totalServiceTime);
			
			schedule.addProcess(processID, info.totalServiceTime);
			
			if(priorityProcessQueue.size() == 0) {
				processExecution.switchToProcess(processID);
			}
			
			priorityProcessQueue.add(spn);
			
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
		switch (policy) {
		
		case FCFS:
			processQueue.remove();
			if(processQueue.size() > 0) {
				processExecution.switchToProcess(processQueue.element());
			}
			break;
		case RR:
			
			processQueue.remove();
			if(processQueue.size() > 0) {
				
				
				//info.elapsedExecutionTime = 0;
				//processAdded(processQueue.element());
				try {
					timer.join();
					processExecution.switchToProcess(processQueue.element());
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} 
			break;
		case SPN:
			SPNSchedule sched = new SPNSchedule(processID, schedule.getTimeForId(processID));
			
			priorityProcessQueue.remove(sched);
			
			if(!priorityProcessQueue.isEmpty()) {	
				processExecution.switchToProcess(priorityProcessQueue.peek().processID);
			}
			
			break;
		}
		
		
	}
}
