package pv4;

/*
 * Micko je kupio pet kosilica za travu sa idejom da ih iznajmljuje i tako
 * zaradi nesto para.
 *
 * Napisati program koji simulira jedan dan iznajmljivanja kosilica. Program
 * kreira 25 prekidivih i 25 neprekidivih musterija i pokrece ih. Posle 60
 * sekundi, glavna nit prekida ostale i po zavrsetku svih njih, stampa kolika
 * bi bila zarada. (5 poena)
 *
 * Prekidive musterije, odmah po pokretanju, generisu nasumicnu duzinu vremena
 * iznajmljivanja izmedju 10 i 20 sekundi. Potom iznajmljuju kosilicu, koriste
 * je zadato vreme, i na kraju vracaju pre zavrsetka svog rada. Ako ih neko
 * prekine u toku cekanja na kosilicu ili u toku koriscenja iste, odmah
 * prekidaju svoj rad, pazeci da i u tom slucaju vrate kosilicu ako su je
 * iznajmili. (5 poena)
 *
 * Neprekidive musterije su implementirane na isti nacin kao i prekidive, sa
 * jedinom razlikom da ne reaguju na prekide i uvek ce sacekati svoj red da
 * iznajme kosilicu i uvek ce pokositi svu travu koju su zamislili. (5 poena)
 *
 * Sinhronizovati iznajmljivanje tako da se ne moze iznajmiti vise od 5 kosilica
 * u isto vreme. (5 poena)
 *
 * Korektno voditi zaradu prilikom iznajmljivanja ako je cena 20 dinara po
 * sekundi. Takodje, izracunati i koliko bi bila zarada da je cena bila 50
 * dinara po sekundi. (5 poena)
 *
 * Obratiti paznju na elegantnost i objektnu orijentisanost realizacije i stil
 * resenja. Za program koji se ne kompajlira, automatski se dobija 0 poena bez
 * daljeg pregledanja.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Mile {
    private int brojDostupnih;
    private int zarada;

    public Mile() {
        this.brojDostupnih = 5;
        this.zarada = 0;
    }

    public synchronized void iznajmiP() throws InterruptedException {
        while(brojDostupnih < 1) {
            wait();
        }
        brojDostupnih--;
    }

    public synchronized void iznajmiNP() {
        while(brojDostupnih < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                // prekinut
            }
        }
        brojDostupnih--;
    }

    public synchronized void vrati(int z) {
        dodajZaradu(z / 1000);
        brojDostupnih++;
        notify();
    }

    private void dodajZaradu(int z) {
        zarada += (z * 20L);
    }

    public void ispisiZaradu() {
        System.out.println("Ukupna zarada je: " + zarada);
        System.out.println("Da je cena po sekundi bila 50 dinara, zarada bi bila: " + zarada * 2.5);
    }
}

class Prekidivi extends Thread {

    Mile m;
    int vremeKosenja = 0;

    public Prekidivi(Mile m) {
        this.m = m;
    }

    @Override
    public void run() {
        try {
            while(!interrupted()) {
                m.iznajmiP();
                try {
                    Random r = new Random();
                    vremeKosenja = r.nextInt(10000, 20000);
                    Thread.sleep(vremeKosenja);
                } finally {
                    m.vrati(vremeKosenja);
                }
            }
        } catch(Exception ex) {

        }
    }
}

class Neprekidivi extends Thread {

    private Mile m;
    private int vremeSpavanja = 0;

    public Neprekidivi(Mile m) {
        this.m = m;
    }

    @Override
    public void run() {
        try {
            while(true) {
                m.iznajmiNP();
                try {
                    Random r = new Random();
                    vremeSpavanja = r.nextInt(10000, 20000);
                    Thread.sleep(vremeSpavanja);

                } finally {
                    m.vrati(vremeSpavanja);
                }
            }
        } catch(Exception ex) {

        }
    }
}

public class Program {
    public static void main(String[] args) throws InterruptedException {
        List<Prekidivi> prekidivi = new ArrayList<>();
        List<Neprekidivi> neprekidivi = new ArrayList<>();
        Mile m = new Mile();
        for(int i = 0; i < 25; i++) {
            Prekidivi p = new Prekidivi(m);
            p.start();
            Neprekidivi np = new Neprekidivi(m);
            np.start();
            prekidivi.add(p);
            neprekidivi.add(np);
        }
        Thread.sleep(60000);
        for(int i = 0; i < 25; i++) {
            prekidivi.get(i).interrupt();
            neprekidivi.get(i).interrupt();
        }

        m.ispisiZaradu();

    }
}
