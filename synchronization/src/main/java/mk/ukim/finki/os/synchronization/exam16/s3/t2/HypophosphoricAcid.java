package mk.ukim.finki.os.synchronization.exam16.s3;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

import java.util.Date;
import java.util.HashSet;

public class HypophosphoricAcid {


    static Semaphore oxygen;
    static Semaphore phosphorus;
    static Semaphore hydrogen;

    static Semaphore phosphorusHere;
    static Semaphore hydrogenHere;

    static Semaphore ready;
    static Semaphore finished;

    static Semaphore lock;

    static int countOxygen;
    public static void init(){
        phosphorus = new Semaphore(2);
        oxygen = new Semaphore(6);
        hydrogen = new Semaphore(4);

        phosphorusHere = new Semaphore(0);
        hydrogenHere = new Semaphore(0);

        ready = new Semaphore(0);
        finished = new Semaphore(0);

        lock = new Semaphore(1);

        countOxygen = 0;
    }
    public static class Phosphorus extends TemplateThread {

        public Phosphorus(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            phosphorus.acquire();
            phosphorusHere.release();
            ready.acquire();
            state.bond();
            finished.release();
        }

    }

    public static class Hydrogen extends TemplateThread {

        public Hydrogen(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            hydrogen.acquire();
            hydrogenHere.release();
            ready.acquire();
            state.bond();
            finished.release();
        }

    }

    public static class Oxygen extends TemplateThread {

        public Oxygen(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            oxygen.acquire();

            lock.acquire();
            countOxygen++;
            if(countOxygen == 6){
                hydrogenHere.acquire(4);
                phosphorusHere.acquire(2);
                ready.release(12);
            }
            lock.release();

            ready.acquire();
            state.bond();
            finished.release();

            lock.acquire();
            countOxygen--;
            if(countOxygen == 0){
                finished.acquire(12);
                state.validate();
                oxygen.release(6);
                hydrogen.release(4);
                phosphorus.release(2);
            }
            lock.release();
        }
    }


  static HypophosphoricAcidState state = new HypophosphoricAcidState();

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      run();
    }
  }

  public static void run() {
    try {
      int numRuns = 1;
      int numScenarios = 100;

      HashSet<Thread> threads = new HashSet<Thread>();

      for (int i = 0; i < numScenarios; i++) {
        for (int j = 0; j < state.O_ATOMS; j++) {
          Oxygen o = new Oxygen(numRuns);
          threads.add(o);
        }
        for (int j = 0; j < state.H_ATOMS; j++) {
          Hydrogen h = new Hydrogen(numRuns);
          threads.add(h);
        }

        for (int j = 0; j < state.P_ATOMS; j++) {
          Phosphorus p = new Phosphorus(numRuns);
          threads.add(p);
        }

      }

      init();

      ProblemExecution.start(threads, state);
      System.out.println(new Date().getTime());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
