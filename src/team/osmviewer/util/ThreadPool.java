/********************************************************************************
 *
 * Open Street Map Viewer
 *
 ********************************************************************************
 *		R E V I S I O N   H I S T O R Y
 ********************************************************************************
 *
 * Date        	Author  	  Description
 * ---------    ---------  	  ---------------------------------------------------
 * JUN 12		team		  Initial Version v0.1
 *
 *
 *******************************************************************************/
package team.osmviewer.util;

import java.util.LinkedList;



public class ThreadPool {
	private LinkedList<Runnable> taskQueue;
	private Thread[] threadArray;
	private boolean keepRunning = true;
	
	public ThreadPool(int numberOfThreads){
		taskQueue = new LinkedList<Runnable>(); //will contain all the tasks
												//that are queued to be run
		threadArray = new Thread[numberOfThreads]; //contains the threads, which will be kept
												   //the entire time until ThreadPool is closed
		for(int i=0; i < numberOfThreads; i++){ //start all threads, even if they still don't have anything to do
			threadArray[i] = new MyThread();
			threadArray[i].start(); 
		}
	}
	
	public void execute(Runnable task){ //puts a task in the taskQueue, where it will wait
		                                //for a thread to get it and run it
		taskQueue.addLast(task); //task will be added at the end of the taskQueue
								 //(first in, first out)
		synchronized (taskQueue){ 
			taskQueue.notify(); //tell a thread that there's a task in the taskQueue
		}
	}
	
	public void close(){
		keepRunning = false; //with this variables set to false, all threads will stop running
							 //when they get to the next "while(keepRunning)" loop
	}
	
	private class MyThread extends Thread{
		private Runnable task;
		
		public MyThread(){			
		}
		
		public void run(){
			while(keepRunning){ //keep running until ThreadPool is closed
				synchronized(taskQueue){
						while(taskQueue.isEmpty()){
							try {
								taskQueue.wait(); // wait for a new task to be put on the taskQueue
												  // this "while" will be blocking the taskQueue for the other threads
												  // but that's OK because it will only block while taskQueue is empty
							} catch (InterruptedException e) {
							} 
						}
						task = taskQueue.pop(); //get the task in the first position
			            					    //of the taskQueue 
				}
				task.run(); //it will only get to this line of code
							//when it has a task to run
							//this line has to be outside the "synchronized" block, so other threads
				            //can access the taskQueue while this thread is running this task
			} 
		} //close run
	} //close MyThread
}
