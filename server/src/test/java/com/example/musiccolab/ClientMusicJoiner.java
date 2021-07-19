package com.example.musiccolab;

public class ClientMusicJoiner implements Runnable {

    private Thread mainThread;
    private Thread thread = null;
    CommunicationHandlingMusicJoiner client;
    short action;
    public int test;

    public ClientMusicJoiner(int test, Thread thread, short action){
        this.action = action;
        mainThread = thread;
        this.test= test;
    }

    @Override
    public void run() {
        if(test == 1)
            client = new CommunicationHandlingMusicJoiner(Thread.currentThread(), test);
        else
            client = new CommunicationHandlingMusicJoiner(mainThread, test);
        client.username = "leon";
        client.password = "123";
        client.email = "leon@gmail.com";
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
        if(test == 1 || test == 77){
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.stop();
        }

        Thread.currentThread().stop();
    }

    public void start(){
        thread = new Thread(this, "client");
        thread.start();
    }
}
