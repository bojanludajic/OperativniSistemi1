package pv3;

import java.util.Random;

public class Program {

    static class Kuhinja {
        private int hleb = 0;
        private int tofu = 0;
        private int povrce = 0;
        private int potaz = 0;
        private int zarada = 0;

        public synchronized void napraviSendvic() throws InterruptedException {
            while(hleb < 2 || tofu < 1 || povrce < 100) {
                wait();
            }

            hleb -= 2;
            tofu -= 1;
            povrce -= 100;
            zarada += 230;
        }

        public synchronized void napraviPotaz() throws InterruptedException {
            while(potaz < 500 || hleb < 1) {
                wait();
            }

            hleb -= 1;
            potaz -= 500;
            zarada += 340;
        }

        public synchronized void napraviTofu() throws InterruptedException {
            while(tofu < 1 || povrce < 300) {
                wait();
            }
            tofu -= 1;
            povrce -= 300;
            zarada += 520;
        }

        public synchronized void dodajHleb() {
            hleb += 6;
            notifyAll();
        }

        public synchronized void dodajTofu() {
            tofu += 1;
            notifyAll();
        }

        public synchronized void dodajPotaz() {
            potaz += 10;
            notifyAll();
        }

        public synchronized void dodajPovrce() {
            povrce += 1000;
            notifyAll();
        }

        public void zaradjeno() {
            System.out.println("Ukupna zarada: " + zarada);
        }
    }


    static class Konobarica extends Thread {
        Kuhinja k;
        int napravi;
        Random r = new Random();

        public Konobarica(Kuhinja k) {
            this.k = k;
            napravi = r.nextInt(3);
        }

        @Override
        public void run() {
            while(!interrupted()) {
                try {
                    Thread.sleep(100);
                    switch(napravi) {
                        case 0:
                            k.napraviPotaz();
                            break;
                        case 1:
                            k.napraviSendvic();
                            break;
                        case 2:
                            k.napraviTofu();
                            break;
                    }
                    napravi = r.nextInt(3);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    static class Sekac extends Thread {
        Kuhinja k;

        public Sekac(Kuhinja k) {
            this.k = k;
        }

        @Override
        public void run() {
            try {
               while(!interrupted()) {
                   Thread.sleep(600);
                   k.dodajPovrce();
               }
            } catch(Exception ex) {
                interrupt();
            }
        }
    }

    static class Kuvar extends Thread {
        Kuhinja k;

        public Kuvar(Kuhinja k) {
            this.k = k;
        }

        @Override
        public void run() {
            try {
                while(!interrupted()) {
                    Thread.sleep(2400);
                    k.dodajPotaz();
                }
            } catch(Exception ex) {
                interrupt();
            }
        }
    }

    static class Pekar extends Thread {
        Kuhinja k;

        public Pekar(Kuhinja k) {
            this.k = k;
        }

        @Override
        public void run() {
            try {
                while(!interrupted()) {
                    Thread.sleep(600);
                    k.dodajHleb();
                }
            } catch(Exception ex) {
                interrupt();
            }
        }
    }

    static class Pekac extends Thread {
        Kuhinja k;

        public Pekac(Kuhinja k) {
            this.k = k;
        }

        @Override
        public void run() {
            try {
                while(!interrupted()) {
                    Thread.sleep(600);
                    k.dodajTofu();
                }
            } catch(Exception ex) {
                interrupt();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Kuhinja k = new Kuhinja();

        Konobarica Rada =  new Konobarica(k);
        Konobarica Dara = new Konobarica(k);
        Sekac Miki = new Sekac(k);
        Kuvar Mica = new Kuvar(k);
        Pekar Joki = new Pekar(k);
        Pekac Vule = new Pekac(k);
        Pekac Gule = new Pekac(k);

        Rada.start();
        Dara.start();
        Miki.start();
        Mica.start();
        Joki.start();
        Vule.start();
        Gule.start();

        Thread.sleep(6000);

        Rada.interrupt();
        Dara.interrupt();
        Miki.interrupt();
        Mica.interrupt();
        Joki.interrupt();
        Vule.interrupt();
        Gule.interrupt();

        k.zaradjeno();
    }

}
