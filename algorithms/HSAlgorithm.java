
package hs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class HSAlgorithm {
  int n = -1;
  int [] ids;
  int [] leader;

  ArrayList<ArrayBlockingQueue<String>> toLeft;
  ArrayList<ArrayBlockingQueue<String>> toRight;
  ArrayList<ArrayBlockingQueue<String>> toMaster;
  ArrayList<ArrayBlockingQueue<String>> toProcess;

  public class Process extends Thread {
    int id = -9999;
    int left = -1;
    int right = -1;
    int index = -1;

    public Process (int index, int id, int left, int right) {
      this.id = id;
      this.left = left;
      this.right = right;
      this.index = index;
    }

    private void writeToLeft (String message) {
      try {
        toLeft.get(left).put(message);
      } catch (InterruptedException e) {
        System.err.println("Write from process " + index
          + " to process " + left + " failed.");
        System.exit(0);
      }
    }

    private void writeToRight (String message) {
      try {
        toRight.get(right).put(message);
      } catch (InterruptedException e) {
        System.err.println("Write from process " + index
          + " to process " + right + " failed.");
        System.exit(0);
      }
    }

    private void writeToMaster (String message) {
      try {
        toMaster.get(index).put(message);
      } catch (InterruptedException e) {
        System.err.println("Write from process " + index
          + " to master failed.");
        System.exit(0);
      }
    }

    private String readFromLeft () {
      try {
        return toRight.get(index).take();
      } catch (InterruptedException e) {
        System.err.println("Read by process " + index
          + " from process " + left + " failed.");
        System.exit(0);
        return "";
      }
    }

    private String readFromRight () {
      try {
        return toLeft.get(index).take();
      } catch (InterruptedException e) {
        System.err.println("Read by process " + index
          + " from process " + right + " failed.");
        System.exit(0);
        return "";
      }
    }

    private String readFromMaster () {
      try {
        return toProcess.get(index).take();
      } catch (InterruptedException e) {
        System.err.println("Read by process " + index
          + " from process master failed.");
        System.exit(0);
        return "";
      }
    }
    
    private void writeToProcess(String message)
    {
    	try {
            toProcess.get(index).put(message);
          } catch (InterruptedException e) {
            System.err.println("Write to process " + index
              + " from master " + " failed.");
            System.exit(0);
          }
    }
    
   /* private String readFromProcess()
    {
    	try
    	{
    		return toMaster.get(index).take();
    	}
    	catch(InterruptedException e)
    	{
    		System.err.println("Read by master " 
    		          + " from process" + index + " master failed.");
    	}
    	return "";
    }*/
    // Method to create a message of the format uid, in/out, hopCount.
    private String createMessage(int id, String dir, int hop)
    {
    	StringBuilder sb = new StringBuilder(3);
    	sb.append(id + " ");
    	sb.append(dir + " ");
    	sb.append(hop);
    	return sb.toString();
    }
    
    // Method to check is the message is valid 
    private boolean isValid(String s)
    {
    	if(s.length()<=1)
    	{
    		return false;
    	}
    	String[] message = s.split(" ");
    	
    	if(Integer.valueOf(message[0]) < 0 && message[1] != null && Integer.valueOf(message[2]) < 0)
    	{
    		return false;
    	}
    	return true;
    }

    @Override
    public void run () 
  {
      // process code goes here!
      // use (readFrom/writeTo)Left,
      // (readFrom/writeTo)Right, and
      // (readFrom/writeTo)Master functions
      // to do things!
      // i.e. wait for the starting signal
      // String startingSignal = readFromMaster();
    	int phase = 0;
    	int u = this.id; 
    	
    	if(readFromMaster() == "start"){
    	
    	writeToLeft(createMessage(id,"out",1));
    	writeToRight(createMessage(id,"out",1));
    	writeToMaster("-1");
    	}
    	
    	
    	while(true)
    	{
    		if(readFromMaster() == "start" )
    		{
    			run();
    		}
    		
    		String fromLeft = readFromLeft();
    		String[] messageFromLeft = fromLeft.split(" ");
    		
    		if(isValid(readFromLeft()))
    		{
    			if(Integer.valueOf(messageFromLeft[0]) > u && Integer.valueOf(messageFromLeft[2])>1)
    			{
    				writeToRight(createMessage(Integer.valueOf(messageFromLeft[0]), "out", Integer.valueOf(messageFromLeft[2])-1 ));
    			}
    			else if(Integer.valueOf(messageFromLeft[0]) > u && Integer.valueOf(messageFromLeft[2]) == 1)
    			{
    				writeToLeft(createMessage(Integer.valueOf(messageFromLeft[0]), "out", 1 ));
    			}
    			else if(Integer.valueOf(messageFromLeft[0]) == u)
    			{
    				writeToMaster(String.valueOf(u));
    				return;
    			}
    		}
    		
    		String fromRight = readFromRight();
    		String[] messageFromRight = fromRight.split(" ");
    		//Checking if the message is valid which is received from both left and right 
    		if(isValid(readFromRight()))
    		{
    			if(Integer.valueOf(messageFromRight[0]) > u && Integer.valueOf(messageFromRight[2])>1)
    			{
    				writeToLeft(createMessage(Integer.valueOf(messageFromRight[0]), "out", Integer.valueOf(messageFromRight[2])-1 ));
    			}
    			else if(Integer.valueOf(messageFromRight[0]) > u && Integer.valueOf(messageFromRight[2]) == 1)
    			{
    				writeToRight(createMessage(Integer.valueOf(messageFromRight[0]), "out", 1 ));
    			}
    			else if(Integer.valueOf(messageFromRight[0]) == u)
    			{
    				writeToMaster(String.valueOf(u));
    				return;
    			}
    		}
    		if(isValid(readFromLeft()))
    		{
    			if(messageFromLeft[1] == "in" && Integer.valueOf(messageFromLeft[2]) == 1 && Integer.valueOf(messageFromLeft[0])!= u)
    			{
    				writeToRight(createMessage(Integer.valueOf(messageFromLeft[0]), "in", 1));
    			}
    		}
    		
    		if(isValid(readFromRight()))
    		{
    			if(messageFromRight[1] == "in" && Integer.valueOf(messageFromRight[2]) == 1 && Integer.valueOf(messageFromRight[0])!= u)
    			{
    				writeToLeft(createMessage(Integer.valueOf(messageFromRight[0]), "in", 1));
    			}
    		}
    		
    		if(isValid(readFromLeft()) && isValid(readFromRight()))
    		{
    			if((messageFromRight[1] == "in" && messageFromLeft[1] == "in") && (Integer.valueOf(messageFromLeft[2]) == 1 && Integer.valueOf(messageFromRight[2]) == 1))
    			{
    				phase++;
    				writeToRight(createMessage(u, "out",  (int) Math.pow(2, phase)));
    				writeToRight(createMessage(u, "out", (int) Math.pow(2, phase)));
    			}
    		}
    	}
    }
}

  public HSAlgorithm (int n, int [] ids) {
    this.n = n;
    this.ids = ids;
    this.leader = new int [n];

    toLeft = new ArrayList<ArrayBlockingQueue<String>>(n);
    toRight = new ArrayList<ArrayBlockingQueue<String>>(n);
    toMaster = new ArrayList<ArrayBlockingQueue<String>>(n);
    toProcess = new ArrayList<ArrayBlockingQueue<String>>(n);

    for (int i = 0; i < n; i++) {
      leader[i] = -9999;

      toLeft.add(new ArrayBlockingQueue<String>(1));
      toRight.add(new ArrayBlockingQueue<String>(1));
      toMaster.add(new ArrayBlockingQueue<String>(1));
      toProcess.add(new ArrayBlockingQueue<String>(1));
    }
  }

  public void start () {
    Thread [] processes = new Thread [n];
    int left, right, k = 0;

    for (int i = 0; i < n; i++) {
      left = i == 0 ? n - 1 : i - 1;
      right = i == n - 1 ? 0 : i + 1;
      processes[i] = new Process(i, ids[i], left, right);
      processes[i].start();
    }

    // master code goes here!
    // at this point, processes all exist!

    // to read from process i, use
    // toMaster.get(i).take(); // returns a string
    // to write to process i, use
    // toProcess.get(i).put(data); // for some string `data` parameter
    while(k < leader.length)
    {
    	for(int i=0; i< n; i++)
    	{
    		if(leader[i] < 0)
    		{
    			writeToProcess(i,"start");
    			
    		}
    	}
    	
    	for(int i=0; i<n; i++)
    	{
    		if(leader[i] < 0)
    		{
    			String meassage = readFromProcess(i);
    			String[] parsedMessage = meassage.split(" ");
    			int messageValue = Integer.valueOf(parsedMessage[i]);
    			if(messageValue > -1)
    			{
    				leader[i] = messageValue;
    			}
    		}
    		// when leader array is full after the first round write to all the process to start the next 
    		//round
    		if(i>=n)
    		{
    			for(int j= 0; j< n; j++){
    			writeToProcess(j, "start");
    			}
    		}
    	}
    	k++;
    }
 }

  private void writeToProcess(int index,String message) 
{
	  try 
	  {
          toProcess.get(index).put(message);
        } catch (InterruptedException e) {
          System.err.println("Write to process " + index
            + " from master " + " failed.");
          System.exit(0);
        }
}

private String readFromProcess(int index) {
	  try
  	{
  		return toMaster.get(index).take();
  	}
  	catch(InterruptedException e)
  	{
  		System.err.println("Read by master " 
  		          + " from process" + index + " master failed.");
  	}
  	return "";
}


public int getLeaderId () {
    for (int i = 0; i < n - 1; i++) {
      if (leader[i] != leader[i + 1]) {
        System.err.println("Multiple leaders elected! HS algorithm failed.");
        return -9999;
      }
    }

    return leader[0];
  }
}

