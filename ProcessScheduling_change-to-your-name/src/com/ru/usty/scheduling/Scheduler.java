package com.ru.usty.scheduling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

public class Scheduler {
	/// ----- PUBLIC ----- ///
		// Variables
		public static Thread timer;
		public static boolean rrMayDie = false;
		public static long startTime;
		public static long currTime;
		public static boolean finished;
		
		// Semaphores
		public static Semaphore switchMutexParent = null;

	/// ----- PRIVATE ----- ///
		// Constants
		private final static int NUMBER_OF_FB_PQ = 7;
		private final static int INITIAL_QUEUE = 0;
		
		// Variables
		private static long avgTurnaroundTime;
		private static long avgRespnseTime;
		private static int lastRunningProcessID;
		//private static int quantum; // REMOVE
		
		// Enums
		private static Policy policy;
		
		// Classes
		private static ProcessExecution processExecution;
		private static ProcessInfo info;
		private static SPNSchedule scheduleSPN;
		private static SRTSchedule scheduleSRT;
		private static FeedbackProcessInfo lastRunningProcess;
	
		// Queues
		private static PriorityQueue<SPNSchedule> priorityProcessQueueSPN;
		private static PriorityQueue<SRTSchedule> priorityProcessQueueSRT;
		private static Queue<Integer> processQueue;
		
		// Lists
		private static ArrayList< Queue<FeedbackProcessInfo>> FBprocessQueues;
		private static LinkedList<Integer> linkedList;
		private static LinkedList<Long> turnaroundArrArrivalTime;
		private static LinkedList<Long> turnaroundArrCompletionTime;
		private static LinkedList<Long> responseArrArrivalTime;		
		
		// Threads
		private static Thread thread = null;
		
		// Semaphores
		private static Semaphore switchMutex = null;
		
		

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		Scheduler.processExecution = processExecution;
		processQueue = new LinkedList<Integer>();
	}
	
	public static void nextQueue() {
		
		switch(policy) {
		case RR:	// Round Robin
			if(processQueue.size() > 1) {
                if(lastRunningProcessID == processQueue.element()) { 
                    int temp = processQueue.remove();
                    processQueue.add(temp);
                    processExecution.switchToProcess(processQueue.element());
                    lastRunningProcessID = processQueue.element();
                }else {
                    processExecution.switchToProcess(processQueue.element());
                    lastRunningProcessID = processQueue.element();
                }
            }
            else if(processQueue.size() == 1) {
                processExecution.switchToProcess(processQueue.element());
                lastRunningProcessID = processQueue.element();
            }
            else {
            	finished = true;
            }
			startTime = System.currentTimeMillis(); 
			break;
		case FB:	// Feedback
			if(lastRunningProcess != null) {
				Queue<FeedbackProcessInfo> lastRunningQueue = FBprocessQueues.get(lastRunningProcess.getQueueID());
				if(!lastRunningQueue.isEmpty()) {
					if( lastRunningProcess == lastRunningQueue.element()) {
						FeedbackProcessInfo tmp = lastRunningQueue.element();
						lastRunningQueue.remove();
						if(tmp.getQueueID() < NUMBER_OF_FB_PQ-1) {
							
							tmp.setQueueID(tmp.getQueueID() + 1);
						}	
						FBprocessQueues.get(tmp.getQueueID()).add(tmp);
						System.out.println("moved to query nr. " + tmp.getQueueID() + "===========================================");
					}
				}
			}
			
			for( Queue<FeedbackProcessInfo> queue : FBprocessQueues) {
				if(!queue.isEmpty()) {
					processExecution.switchToProcess(queue.element().getID());
					lastRunningProcess = queue.element();
					startTime = System.currentTimeMillis(); 
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
		
		if(linkedList.size() == 0) {finished = true;return;} // Nothing in list, do nothing.
		
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
		Scheduler.policy = policy;
		//Scheduler.quantum = quantum;
		System.out.println("policy: " + policy);
		System.out.println("quantum: " + quantum);
		
		rrMayDie = true;
		if(thread != null) {
			if(thread.isAlive()) {
				try {
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
			processQueue = new LinkedList<Integer>();
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			processQueue = null;
			
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			
			processQueue = new LinkedList<Integer>();
			switchMutex = new Semaphore(1);
			switchMutexParent = new Semaphore(1);
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			priorityProcessQueueSPN = new PriorityQueue<SPNSchedule>();
			scheduleSPN = new SPNSchedule();
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			priorityProcessQueueSRT = new PriorityQueue<SRTSchedule>();
			scheduleSRT= new SRTSchedule();
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			linkedList = new LinkedList<Integer>();
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);		
			FBprocessQueues = null;
			switchMutex = new Semaphore(1);	
			turnaroundArrArrivalTime = new LinkedList<Long>();
			turnaroundArrCompletionTime = new LinkedList<Long>();
			responseArrArrivalTime = new LinkedList<Long>();
			finished = false;
			avgTurnaroundTime = 0;
			avgRespnseTime = 0;
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();
			FBprocessQueues = new ArrayList<Queue<FeedbackProcessInfo>>();
			for(int i = 0; i < NUMBER_OF_FB_PQ; i++) {
				FBprocessQueues.add(new LinkedList<FeedbackProcessInfo>());
			}
			switchMutex = new Semaphore(1);
			switchMutexParent = new Semaphore(1);
			thread = new Thread(new RoundRobinTimer(quantum));
			thread.start();
			break;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processAdded(int processID) {
		info = processExecution.getProcessInfo(processID);
		System.out.println("PROCESS ID: " + processID);
		System.out.println("total time: " + info.totalServiceTime);
		System.out.println("Execution time: " + info.elapsedExecutionTime);
		System.out.println("waiting time: " + info.elapsedWaitingTime);
		
		switch(policy) {
		case FCFS:	// First come first served
			try {
				responseArrArrivalTime.get(processID);
				responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				responseArrArrivalTime.add(processID, System.currentTimeMillis());	
			}

			try {
				turnaroundArrArrivalTime.get(processID);
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
			}
			finished = false;
			if(processQueue.size() == 0) {
				processExecution.switchToProcess(processID);
			}
			
			processQueue.add(processID);
			break;
		case RR:	// Round robin
			try {
				
				switchMutex.acquire();
					try {
						responseArrArrivalTime.get(processID);
						responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
					} catch( IndexOutOfBoundsException e) {
						responseArrArrivalTime.add(processID, System.currentTimeMillis());	
					}
	
					try {
						turnaroundArrArrivalTime.get(processID);
						turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
					} catch( IndexOutOfBoundsException e) {
						turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
					}
					finished = false;
					processQueue.add(processID);	
				switchMutex.release();
				switchMutexParent.acquire();
				if(processQueue.size() == 1) {
					nextQueue();
				}
				switchMutexParent.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case SPN:	// Shortest process next
			try {
				responseArrArrivalTime.get(processID);
				responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				responseArrArrivalTime.add(processID, System.currentTimeMillis());	
			}

			try {
				turnaroundArrArrivalTime.get(processID);
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
			}
			finished = false;
			SPNSchedule spn = new SPNSchedule(processID, info.totalServiceTime);
			scheduleSPN.addProcess(processID, info.totalServiceTime);
			
			if(priorityProcessQueueSPN.size() == 0) {
				processExecution.switchToProcess(processID);
			}
			
			
			priorityProcessQueueSPN.add(spn);
			break;
		case SRT:	// Shortest remaining time
			SRTSchedule srt = new SRTSchedule(processID, info.totalServiceTime-info.elapsedExecutionTime);	
			
			try {
				responseArrArrivalTime.get(processID);
				responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				responseArrArrivalTime.add(processID, System.currentTimeMillis());	
			}

			try {
				turnaroundArrArrivalTime.get(processID);
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
			}
			
			if(!priorityProcessQueueSRT.isEmpty())
			{
				ProcessInfo bestSRTinfo = processExecution.getProcessInfo(priorityProcessQueueSRT.peek().getProcessID());
				priorityProcessQueueSRT.peek().updateRemainingTime(bestSRTinfo.totalServiceTime - bestSRTinfo.elapsedExecutionTime);
			}
			finished = false;
			scheduleSRT.addProcess(processID, info.totalServiceTime-info.elapsedExecutionTime);
	
			if(priorityProcessQueueSRT.isEmpty()) {
				priorityProcessQueueSRT.add(srt);
				processExecution.switchToProcess(processID);
			}else {
				priorityProcessQueueSRT.add(srt);
				processExecution.switchToProcess(priorityProcessQueueSRT.peek().getProcessID());
			}
			break;
		case HRRN:	// Highest response ratio next
			System.out.println("HRRN added process entered!");
			
			try {
				responseArrArrivalTime.get(processID);
				responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				responseArrArrivalTime.add(processID, System.currentTimeMillis());	
			}

			try {
				turnaroundArrArrivalTime.get(processID);
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
			} catch( IndexOutOfBoundsException e) {
				turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
			}
			finished = false;

			if(linkedList.size() == 0) {
				linkedList.add(processID);
				processExecution.switchToProcess(processID);
			}else {
				linkedList.add(processID);
			}
			break;
		case FB:	// Feedback
			System.out.println("FB added process entered!");
			
			try {
				
				switchMutex.acquire();
					try {
						responseArrArrivalTime.get(processID);
						responseArrArrivalTime.add(processID, System.currentTimeMillis() + responseArrArrivalTime.get(processID));
					} catch( IndexOutOfBoundsException e) {
						responseArrArrivalTime.add(processID, System.currentTimeMillis());	
					}
	
					try {
						turnaroundArrArrivalTime.get(processID);
						turnaroundArrArrivalTime.add(processID, System.currentTimeMillis() + turnaroundArrArrivalTime.get(processID));
					} catch( IndexOutOfBoundsException e) {
						turnaroundArrArrivalTime.add(processID, System.currentTimeMillis());	
					}
					finished = false;
					FeedbackProcessInfo feedbackProcessInfo = new FeedbackProcessInfo(processID,INITIAL_QUEUE);
					FBprocessQueues.get(INITIAL_QUEUE).add(feedbackProcessInfo);
				switchMutex.release();
				switchMutexParent.acquire();
				for( Queue<FeedbackProcessInfo> queue : FBprocessQueues) {
					if(!queue.isEmpty()) {
						switchMutexParent.release();
						return;
					}
				}
				nextQueue();
				switchMutexParent.release();
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
		
		
		
		switch(policy) {
		case FCFS:	// First come first served
			turnaroundArrCompletionTime.add(processID, System.currentTimeMillis());
			processQueue.remove();
			if(processQueue.size() > 0) {
				processExecution.switchToProcess(processQueue.element());
			}
			else {
				finished = true;
			}
			break;
		case SPN:	// Shortest process next
			turnaroundArrCompletionTime.add( System.currentTimeMillis());
			SPNSchedule sched = new SPNSchedule(processID, scheduleSPN.getTimeForId(processID));
			priorityProcessQueueSPN.remove(sched);
			if(!priorityProcessQueueSPN.isEmpty()) {	
				processExecution.switchToProcess(priorityProcessQueueSPN.peek().getProcessID());
			}
			else {
				finished = true;
			}
		break;
		case SRT:	// Shortest time remaining
			
			turnaroundArrCompletionTime.add(System.currentTimeMillis());
			priorityProcessQueueSRT.remove();
			if(!priorityProcessQueueSRT.isEmpty()) {	
				processExecution.switchToProcess(priorityProcessQueueSRT.peek().getProcessID());
			}
			else {
				finished = true;
			}
			break;
		case RR:	// Round robin
			
			try {
				
				switchMutex.acquire();
					turnaroundArrCompletionTime.add(System.currentTimeMillis());
					processQueue.remove(processID);
				switchMutex.release();
				switchMutexParent.acquire();
				nextQueue();
				switchMutexParent.release();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}		
			
			
			break;
		case HRRN:	// Highest response ratio next
			System.out.println("HRRN removed process entered!");
			turnaroundArrCompletionTime.add(System.currentTimeMillis());
			linkedList.removeFirst();
			
			nextHRRN();
			break;
		case FB:	// Feedback
			try {
				
				switchMutex.acquire();
					turnaroundArrCompletionTime.add(processID, System.currentTimeMillis());
					FBprocessQueues.get(lastRunningProcess.getQueueID()).remove();
				switchMutex.release();
				switchMutexParent.acquire();
				nextQueue();
				switchMutexParent.release();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}	
		if(finished) { 
			for(int i = 0; i < turnaroundArrArrivalTime.size(); i++) {
				long turnaroundTime = turnaroundArrCompletionTime.get(i) - turnaroundArrArrivalTime.get(i);
				avgRespnseTime += responseArrArrivalTime.get(i);
				avgTurnaroundTime += turnaroundTime;
			}
			avgRespnseTime = avgRespnseTime/responseArrArrivalTime.size();
			avgTurnaroundTime = avgTurnaroundTime/turnaroundArrArrivalTime.size();
			System.out.println("Average Turnaround time: " + avgTurnaroundTime);
			System.out.println("Average Response time: " + avgRespnseTime);
		}
	}
}
