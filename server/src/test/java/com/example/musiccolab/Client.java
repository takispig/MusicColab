package com.example.musiccolab;


public class Client implements Runnable{

    private Thread mainThread;
    public Thread thread = null;
    CommunicationHandling client;
    short action;
    public int test;

    public Client(int test, Thread thread, short action){
        this.action = action;
        mainThread = thread;
        this.test= test;
    }

    @Override
    public void run() {
        if(test == 1)
            client = new CommunicationHandling(Thread.currentThread(), test);
        else
            client = new CommunicationHandling(mainThread, test);
        client.username = "zead";
        client.password = "123";
        client.email = "zead@gmail.com";
        client.question = "du";
        client.toneAction = 1;
        client.toneType = 1;
        client.data = "dataExample2";
        client.lobbyName = "example";
        client.lobbyID = 1;

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
            synchronized (mainThread) {
                mainThread.notify();
            }
        }
    }

    public void start(){
        thread = new Thread(this, "client");
        thread.start();
    }
}
