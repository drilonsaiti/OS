package mk.ukim.finki.os.synchronization.exam17.k1.g1;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

public class MusicBand {
  
    static Semaphore singer;
    static Semaphore guitar;

    static Semaphore singerHere;

    static Semaphore ready;
    static Semaphore finished;

    static Semaphore lock;
    static int countGuitar;
    public static void init() {
        singer = new Semaphore(2);
        guitar = new Semaphore(3);

        singerHere = new Semaphore(0);

        ready = new Semaphore(0);
        finished = new Semaphore(0);

        lock = new Semaphore(1);
        countGuitar = 0;
    }

    public static class GuitarPlayer extends TemplateThread {

        public GuitarPlayer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            guitar.acquire();

            lock.acquire();
            countGuitar++;
            if(countGuitar == 3){
                singerHere.acquire(2);
                ready.release(5);
            }
            lock.release();

            ready.acquire();
            state.play();
            finished.release();

            lock.acquire();
            countGuitar--;
            if(countGuitar == 0){
                finished.acquire(5);
                state.evaluate();
                guitar.release(3);
                singer.release(2);
            }
            lock.release();
        }

    }

    public static class Singer extends TemplateThread {

        public Singer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            singer.acquire();
            singerHere.release();
            ready.acquire();
            state.play();
            finished.release();
        }

    }
  
 static MusicBandState state = new MusicBandState();
  
  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      run();
    }
  }

  public static void run() {
    try {
      Scanner s = new Scanner(System.in);
      int numRuns = 1;
      int numIterations = 100;
      s.close();

      HashSet<Thread> threads = new HashSet<Thread>();

      for (int i = 0; i < numIterations; i++) {
        Singer singer = new Singer(numRuns);
        threads.add(singer);
        GuitarPlayer gp = new GuitarPlayer(numRuns);
        threads.add(gp);
        gp = new GuitarPlayer(numRuns);
        threads.add(gp);
        singer = new Singer(numRuns);
        threads.add(singer);
        gp = new GuitarPlayer(numRuns);
        threads.add(gp);
      }

      init();

      ProblemExecution.start(threads, state);
      System.out.println(new Date().getTime());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
