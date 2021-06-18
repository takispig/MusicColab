package test;

import test.CommunicationHandling;


public class Client implements Runnable{

    private Thread mainThread;
    private Thread thread = null;
    CommunicationHandling client;
    short action;

    public String result = "";
    public int test;

    public Client(int test, Thread thread, short action){
        this.action = action;
        mainThread = thread;
        this.test= test;
    }

    @Override
    public void run() {
        if(test == 0)
            client = new CommunicationHandling(mainThread);
        else if(test == 1)
            client = new CommunicationHandling(Thread.currentThread());
        client.username = "zead";
        client.password = "123";
        client.email = "zead@gmail.com";
        client.toneAction = 1;
        client.toneType = 1;
        client.data = "dataExample2";
        client.lobbyName = "example";
        client.lobbyID = 0;

        client.action = action;

        System.out.println(client.username);

        if (client.threadExist) {
            client.communicationThread.notify();
        } else {
            client.start();
        }
        if(test == 1){
            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Error with waiting of main thread.");
            }
            result = client.result;
            synchronized (mainThread) {
                mainThread.notify();
            }
        }

        client.stop();
        Thread.currentThread().stop();
    }

    public void start(){
        thread = new Thread(this, "client");
        thread.start();
    }
}