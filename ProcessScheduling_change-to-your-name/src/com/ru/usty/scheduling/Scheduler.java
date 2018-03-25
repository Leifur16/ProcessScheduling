package com.ru.usty.scheduling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {

	static ProcessExecution processExecution;
	static Policy policy;
	int quantum;
	public static Thread timer;
	static ProcessInfo info;
	SPNSchedule schedule;
	static FeedbackProcessInfo lastRunningProcess;
	
	public final static int NUMBER_OF_FB_PQ = 7;
	public final int INITIAL_QUEUE = 0;
	
	//Queue<Integer> processQueue;
	PriorityQueue<SPNSchedule> priorityProcessQueue;

	public static boolean rrMayDie = false;
	
	public static Queue<Integer> processQueue;
	public static LinkedList<Integer> linkedList;
	private Thread thread = null;
	public static ArrayList< Queue<FeedbackProcessInfo>> FBprocessQueues;
	
	private static int lastRunningProcessID;
	
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
		
		switch(policy) {
		case RR:	//First-come-first-served
			try {
				switchMutex.acquire();
					if(processQueue.size() > 1) {
	                    info = processExecution.getProcessInfo(processQueue.element());
	                    if(lastRunningProcessID == processQueue.element()) {
	                            
	                            int temp = processQueue.remove();
	                            processQueue.add(temp);
	                            
	                            processExecution.switchToProcess(processQueue.element());
	                            lastRunningProcessID = processQueue.element();
	                    }
	                    else {
	                            processExecution.switchToProcess(processQueue.element());
	                            lastRunningProcessID = processQueue.element();
	                    }
		            }
		            else if(processQueue.size() == 1) {
		                    processExecution.switchToProcess(processQueue.element());
		                    lastRunningProcessID = processQueue.element();
		            }
				switchMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			break;
		case FB:	//Feedback
			try {
				switchMutex.acquire();
					if(lastRunningProcess != null) {
						Queue<FeedbackProcessInfo> lastRunningQueue = FBprocessQueues.get(lastRunningProcess.queueID);
						if(!lastRunningQueue.isEmpty()) {
							if( lastRunningProcess == lastRunningQueue.element()) {
								FeedbackProcessInfo tmp = lastRunningQueue.element();
								lastRunningQueue.remove();
								if(tmp.queueID < NUMBER_OF_FB_PQ-1) {
									tmp.queueID++;
								}	
								System.out.println("----------------------------------------------------------------");
								FBprocessQueues.get(tmp.queueID).add(tmp);
							}
						}
					}
					
					
				switchMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for( Queue<FeedbackProcessInfo> queue : FBprocessQueues) {
				if(!queue.isEmpty()) {
					processExecution.switchToProcess(queue.element().ID);
					lastRunningProcess = queue.element();
					return;
				}
			}

			break;
		default:
			break;
		}
		
			
	}
	
	public void nextHRRN() {

		double responseRatio = 0.0;
		double maxRatio = -1;
		int maxRatioID = -1;
		
		if(linkedList.size() == 0) {return;} // Nothing in list, do nothing.
		
		for (int processID : linkedList) {
			ProcessInfo info = processExecution.getProcessInfo(processID);
            responseRatio = 1 + (info.elapsedWaitingTime  / info.totalServiceTime);

            if(maxRatio < responseRatio) {
            	maxRatioID = processID;
            	maxRatio = responseRatio;
            }
        }
			
		if(maxRatioID == linkedList.element()) { // First in list is highest ratio
			processExecution.switchToProcess(linkedList.element());
		}else { // First in queue is not highest ratio, move to front of list	
			linkedList.remove(linkedList.indexOf(maxRatioID));
			linkedList.addFirst(maxRatioID);
			processExecution.switchToProcess(linkedList.element());
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
			
			linkedList = new LinkedList<Integer>();
			
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
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
			
			switchMutex = new Semaphore(1);
			
			System.out.println("==================== thread was started  IN BEGINNING =====================");
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();
			
			
			FBprocessQueues = new ArrayList<Queue<FeedbackProcessInfo>>();
			for(int i = 0; i < NUMBER_OF_FB_PQ; i++) {
				FBprocessQueues.add(new LinkedList<FeedbackProcessInfo>());
			}
			
	
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
		info = processExecution.getProcessInfo(processID);
		System.out.println("PROCESS ID: " + processID);
		System.out.println("total time: " + info.totalServiceTime);
		System.out.println("Execution time: " + info.elapsedExecutionTime);
		System.out.println("waiting time: " + info.elapsedWaitingTime);
		
		switch(policy) {
		case FCFS:	//First-come-first-served
			if(processQueue.size() == 0) {
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
		case SPN:
			
			SPNSchedule spn = new SPNSchedule(processID, info.totalServiceTime);
			
			schedule.addProcess(processID, info.totalServiceTime);
			
			if(priorityProcessQueue.size() == 0) {
				processExecution.switchToProcess(processID);
			}
			
			priorityProcessQueue.add(spn);
			
			
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("HRRN added process entered!");

			if(linkedList.size() == 0) {
				System.out.println("First time only?????????????????????????????????");
				linkedList.add(processID);
				processExecution.switchToProcess(processID);
			}else {
				linkedList.add(processID);
			}
			break;
		case FB:	//Highest response ratio next
			System.out.println("FB added process entered!");
			
			
			
			try {
				switchMutex.acquire();
					FeedbackProcessInfo feedbackProcessInfo = new FeedbackProcessInfo(processID,INITIAL_QUEUE);
					FBprocessQueues.get(INITIAL_QUEUE).add(feedbackProcessInfo);
				switchMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			


			break;
		default:

			
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

		case SPN:
			SPNSchedule sched = new SPNSchedule(processID, schedule.getTimeForId(processID));
			
			priorityProcessQueue.remove(sched);
			
			if(!priorityProcessQueue.isEmpty()) {	
				processExecution.switchToProcess(priorityProcessQueue.peek().processID);
			}
		break;
			
		case RR:	//Round robin
	
			try {
				switchMutex.acquire();
					processQueue.remove(processID);
				switchMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("HRRN removed process entered!");
			
			linkedList.removeFirst();
			nextHRRN();
			
			break;
		case FB:	//Highest response ratio next
			System.out.println("FB removed process entered!");
			
			try {
				switchMutex.acquire();
					FBprocessQueues.get(lastRunningProcess.queueID).remove();
				switchMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			

			break;
		default:
			break;
		}	
	}
}
