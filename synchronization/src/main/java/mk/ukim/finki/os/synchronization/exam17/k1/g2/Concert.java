package mk.ukim.finki.os.synchronization.exam17.k1.g2;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

import java.util.Date;
import java.util.HashSet;

public class Concert {


  static Semaphore performer;
    static Semaphore bariton;
    static Semaphore tenor;

    static Semaphore tenorbaritonHere;
    static Semaphore baritonHere;
    static Semaphore readyBaritonTenor ;

    static Semaphore ready;
    static Semaphore finished;
    static Semaphore next;
    
    public static void init() {
        performer = new Semaphore(1);
        bariton = new Semaphore(3);
        tenor = new Semaphore(3);

        baritonHere = new Semaphore(0);
        readyBaritonTenor = new Semaphore(0);
        tenorbaritonHere = new Semaphore(0);

        ready = new Semaphore(0);
        finished = new Semaphore(0);
        next = new Semaphore(0);

    }

    public static class Performer extends TemplateThread {

        public Performer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            performer.acquire();
            tenorbaritonHere.acquire(6);

            ready.release(6);
            state.perform();
            finished.acquire(6);

            next.release(6);
            state.vote();
            performer.release();
        }

    }

    public static class Baritone extends TemplateThread {

        public Baritone(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            bariton.acquire();
            baritonHere.release();

            readyBaritonTenor.acquire();
            state.formBackingVocals();
            tenorbaritonHere.release();

            ready.acquire();
            state.perform();
            finished.release();

            next.acquire();
            bariton.release();
        }
    }

    public static class Tenor extends TemplateThread {

        public Tenor(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            tenor.acquire();
            baritonHere.acquire();

            readyBaritonTenor.release();
            state.formBackingVocals();
            tenorbaritonHere.release();

            ready.acquire();
            state.perform();
            finished.release();

            next.acquire();
            tenor.release();

        }

    }

  static ConcertState state = new ConcertState();

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      run();
    }
  }

  public static void run() {
    try {
      int numRuns = 1;
      int numScenarios = 300;

      HashSet<Thread> threads = new HashSet<Thread>();

      for (int i = 0; i < numScenarios; i++) {
        Tenor t = new Tenor(numRuns);
        Baritone b = new Baritone(numRuns);
        threads.add(t);
        if (i % 3 == 0) {
          Performer p = new Performer(numRuns);
          threads.add(p);
        }
        threads.add(b);
      }

      init();

      ProblemExecution.start(threads, state);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
